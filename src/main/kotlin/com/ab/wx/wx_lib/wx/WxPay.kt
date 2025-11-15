package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.config.WxPayConfigProperties
import com.ab.wx.wx_lib.dto.pay.*
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.fn.aes.WxPayAes
import com.ab.wx.wx_lib.vo.pay.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest

class WxPay(
    wxConfigProperties: WxConfigProperties,
    private val payConfig: WxPayConfigProperties
) {
    private val logger = LoggerFactory.getLogger(WxPay::class.java)

    private val restTemplate = getRestTemplate()
    private val mapper = getMapper()

    private val appId = wxConfigProperties.appId
    private val mchId = payConfig.mchid ?: throw IllegalStateException("wx.pay.mchid must be configured")
    private val notifyUrl = payConfig.notifyUrl
    private val refundsNotifyUrl = payConfig.refundsNotifyUrl
    private val serialNo = payConfig.serialNo
    private val apiV3Key = payConfig.apiV3Key ?: throw IllegalStateException("wx.pay.api-v3-key must be configured")
    private val apiHost = payConfig.apiHost.trimEnd('/')

    private val merchantPrivateKey: PrivateKey = loadPrivateKeyFromString(resolvePrivateKeyPem())
    private val certificateCache: MutableMap<String, X509Certificate> = ConcurrentHashMap()
    private var lastCertRefresh: Instant? = null

    private val schema = "WECHATPAY2-SHA256-RSA2048"
    private val signMethod = "SHA256withRSA"

    private val jsApiPayPath = "/v3/pay/transactions/jsapi"
    private val h5PayPath = "/v3/pay/transactions/h5"
    private val certificatesPath = "/v3/certificates"
    private val refundPath = "/v3/refund/domestic/refunds"

    fun genSimplePay(dto: SimplePayDto): JsApiPayRes? {
        val notifyEndpoint = dto.notifyUrl.ifBlank { notifyUrl }
            ?: throw IllegalStateException("notify_url must be provided either on SimplePayDto or wx.pay.notify-url")
        val request = JsApiPayDto(
            appid = appId,
            mchid = mchId,
            description = dto.description,
            out_trade_no = dto.orderNo,
            notify_url = notifyEndpoint,
            amount = JsApiPayAmountDto(total = dto.amount),
            payer = JsApiPayerDto(dto.payOpenid)
        )
        return jsapiPay(request)
    }

    fun jsapiPay(dto: JsApiPayDto): JsApiPayRes? {
        val response = request(HttpMethod.POST, jsApiPayPath, dto, JsApiPayVo::class.java)
        logger.info("JSAPI transaction request: {} -> {}", dto.out_trade_no, response)
        return response?.prepay_id?.let { genJsSign(it, dto.out_trade_no) }
    }

    fun h5Pay(dto: H5PayDto): H5PrepayVo? {
        val response = request(HttpMethod.POST, h5PayPath, dto, H5PrepayVo::class.java)
        logger.info("H5 transaction request: {} -> {}", dto.out_trade_no, response)
        return response
    }

    fun queryTransaction(outTradeNo: String): H5PayDecodeVo? {
        val path = "/v3/pay/transactions/out-trade-no/$outTradeNo?mchid=$mchId"
        return request(HttpMethod.GET, path, null, H5PayDecodeVo::class.java)
    }

    fun queryTransactionById(transactionId: String): H5PayDecodeVo? {
        val path = "/v3/pay/transactions/id/$transactionId?mchid=$mchId"
        return request(HttpMethod.GET, path, null, H5PayDecodeVo::class.java)
    }

    fun closeOrder(outTradeNo: String) {
        val path = "/v3/pay/transactions/out-trade-no/$outTradeNo/close"
        val body = mapOf("mchid" to mchId)
        request(HttpMethod.POST, path, body, String::class.java)
    }

    fun refund(refundPayDto: RefundPayDto): String? {
        val payload = refundPayDto.copy(notify_url = refundPayDto.notify_url ?: refundsNotifyUrl)
        return request(HttpMethod.POST, refundPath, payload, String::class.java)
    }

    fun simpleRefunds(dto: SimpleRefundsDto): String? {
        return refund(
            RefundPayDto(
                out_refund_no = dto.refundsOrderId,
                out_trade_no = dto.orderId,
                amount = RefundsAmount(refund = dto.refundsMoney, total = dto.totalMoney),
                notify_url = refundsNotifyUrl
            )
        )
    }

    fun refundsWithPromotion(dto: RefundsWithPromotionDto): String? {
        return refund(
            RefundPayDto(
                out_refund_no = dto.refundsOrderId,
                out_trade_no = dto.orderId,
                amount = RefundsAmount(refund = dto.refundsMoney, total = dto.totalMoney),
                notify_url = refundsNotifyUrl
            )
        )
    }

    fun queryRefund(outRefundNo: String): H5RefundsDecodeVo? {
        val path = "$refundPath/$outRefundNo"
        return request(HttpMethod.GET, path, null, H5RefundsDecodeVo::class.java)
    }

    fun callbackFn(request: HttpServletRequest): H5PayDecodeVo? {
        return parseNotification(request, H5PayDecodeVo::class.java)
    }

    fun refundsCallbackFn(request: HttpServletRequest): H5RefundsDecodeVo? {
        return parseNotification(request, H5RefundsDecodeVo::class.java)
    }

    @Deprecated("apiV3Key is now read from WxPayConfigProperties")
    fun callbackFn(request: HttpServletRequest, apiV3Key: String): H5PayDecodeVo? = callbackFn(request)

    @Deprecated("apiV3Key is now read from WxPayConfigProperties")
    fun refundsCallbackFn(request: HttpServletRequest, apiV3Key: String): H5RefundsDecodeVo? = refundsCallbackFn(request)

    fun verifySignature(timestamp: String?, nonce: String?, body: String, signature: String?, serial: String?): Boolean {
        if (signature.isNullOrBlank() || serial.isNullOrBlank()) {
            logger.warn("Missing signature headers, serial={}, signature={}", serial, signature)
            return false
        }
        val certificate = try {
            ensureCertificate(serial)
        } catch (ex: Exception) {
            logger.error("Cannot load certificate for serial {}", serial, ex)
            return false
        }
        return try {
            val verifier = Signature.getInstance(signMethod)
            verifier.initVerify(certificate.publicKey)
            val message = if (!timestamp.isNullOrBlank() && !nonce.isNullOrBlank()) {
                genPaySign(timestamp, nonce, body)
            } else {
                body
            }
            verifier.update(message.toByteArray(StandardCharsets.UTF_8))
            verifier.verify(Base64.getDecoder().decode(signature))
        } catch (ex: Exception) {
            logger.error("Signature verification failed", ex)
            false
        }
    }

    private fun <T> parseNotification(request: HttpServletRequest, clazz: Class<T>): T? {
        val body = readIns(request.inputStream)
        val timestamp = request.getHeader("Wechatpay-Timestamp")
        val nonce = request.getHeader("Wechatpay-Nonce")
        val signature = request.getHeader("Wechatpay-Signature")
        val serial = request.getHeader("Wechatpay-Serial")
        if (!verifySignature(timestamp, nonce, body, signature, serial)) {
            logger.warn("Invalid notification signature, serial={} body={} ", serial, body)
            return null
        }
        val wrapper = mapper.readValue(body, H5PayVo::class.java)
        val plainText = WxPayAes.decryptToString(
            wrapper.resource.associated_data,
            wrapper.resource.nonce,
            wrapper.resource.ciphertext,
            apiV3Key
        ) ?: return null
        return mapper.readValue(plainText, clazz)
    }

    private fun resolvePrivateKeyPem(): String {
        payConfig.privateKey?.takeIf { it.isNotBlank() }?.let { return it }
        val path = payConfig.keyPath?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("wx.pay.private-key or wx.pay.key-path must be configured")
        FileInputStream(path).use { return readIns(it) }
    }

    private fun loadPrivateKeyFromString(keyString: String): PrivateKey {
        val normalized = keyString.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(normalized))
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    private fun genToken(method: String, pathWithQuery: String, body: String): String {
        val nonceStr = create_pay_nonce()
        val time = create_timestamp()
        val message = genPaySign(method, pathWithQuery, time, nonceStr, body)
        val signature = sign(message.toByteArray(StandardCharsets.UTF_8))
        return "$schema mchid=\"$mchId\",nonce_str=\"$nonceStr\",timestamp=\"$time\",serial_no=\"$serialNo\",signature=\"$signature\""
    }

    private fun sign(message: ByteArray): String {
        val signature = Signature.getInstance(signMethod)
        signature.initSign(merchantPrivateKey)
        signature.update(message)
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    private fun genJsSign(prepayId: String, orderNo: String): JsApiPayRes {
        val time = create_timestamp()
        val nonce = create_pay_nonce()
        val paySign = sign(genPaySign(appId, time, nonce, "prepay_id=$prepayId").toByteArray(StandardCharsets.UTF_8))
        return JsApiPayRes(prepayId = prepayId, timestamp = time, nonceStr = nonce, paySign = paySign, orderId = orderNo)
    }

    private fun <T> request(method: HttpMethod, pathWithQuery: String, body: Any?, responseType: Class<T>): T? {
        val payload = body?.let { mapper.writeValueAsString(it) }
        val token = genToken(method.name, pathWithQuery, payload ?: "")
        val headers = getPayHeaders(token)
        val entity: HttpEntity<*> = if (payload == null) {
            HttpEntity<Any>(headers)
        } else {
            HttpEntity(payload, headers)
        }
        val url = "$apiHost$pathWithQuery"
        val response = restTemplate.exchange(url, method, entity, responseType)
        return response.body
    }

    private fun ensureCertificate(serial: String): X509Certificate {
        certificateCache[serial]?.let { return it }
        if (payConfig.autoUpdateCertificate) {
            val now = Instant.now()
            if (lastCertRefresh == null || now.isAfter(lastCertRefresh!!.plus(payConfig.certificateTtl))) {
                refreshCertificates()
            }
        }
        return certificateCache[serial]
            ?: throw IllegalStateException("Certificate $serial not found, please call refreshCertificates() first")
    }

    private fun refreshCertificates() {
        val response = request(HttpMethod.GET, certificatesPath, null, PayCertResVo::class.java) ?: return
        val cf = CertificateFactory.getInstance("X509")
        response.data.forEach { cert ->
            val encryptCert = cert.encrypt_certificate
            val plain = WxPayAes.decryptToString(
                encryptCert.associated_data,
                encryptCert.nonce,
                encryptCert.ciphertext,
                apiV3Key
            ) ?: return@forEach
            val certificate = cf.generateCertificate(ByteArrayInputStream(plain.toByteArray(StandardCharsets.UTF_8))) as X509Certificate
            certificate.checkValidity()
            certificateCache[cert.serial_no] = certificate
        }
        lastCertRefresh = Instant.now()
    }
}
