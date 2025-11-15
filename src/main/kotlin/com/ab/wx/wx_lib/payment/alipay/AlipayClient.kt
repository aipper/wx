package com.ab.wx.wx_lib.payment.alipay

import com.ab.wx.wx_lib.fn.getMapper
import com.ab.wx.wx_lib.fn.getRestTemplate
import com.ab.wx.wx_lib.fn.readIns
import com.ab.wx.wx_lib.payment.api.CloseOrderCommand
import com.ab.wx.wx_lib.payment.api.CloseOrderResult
import com.ab.wx.wx_lib.payment.api.CreateOrderCommand
import com.ab.wx.wx_lib.payment.api.CreateOrderResult
import com.ab.wx.wx_lib.payment.api.PaymentChannel
import com.ab.wx.wx_lib.payment.api.PaymentClient
import com.ab.wx.wx_lib.payment.api.PaymentClientException
import com.ab.wx.wx_lib.payment.api.PaymentProvider
import com.ab.wx.wx_lib.payment.api.PaymentStatus
import com.ab.wx.wx_lib.payment.api.QueryOrderCommand
import com.ab.wx.wx_lib.payment.api.QueryOrderResult
import com.ab.wx.wx_lib.payment.api.RefundCommand
import com.ab.wx.wx_lib.payment.api.RefundResult
import com.ab.wx.wx_lib.payment.config.AlipayMerchantProperties
import com.ab.wx.wx_lib.payment.config.AlipayProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.io.FileInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap

/**
 * 轻量版支付宝客户端：生成有签名的订单字符串，并支持简单的查询/关单/退款调用。
 */
class AlipayClient(private val properties: AlipayProperties) : PaymentClient {

    private val logger = LoggerFactory.getLogger(AlipayClient::class.java)
    private val mapper = getMapper()
    private val restTemplate: RestTemplate = getRestTemplate()
    private val privateKeyCache = ConcurrentHashMap<String, PrivateKey>()
    private val publicKeyCache = ConcurrentHashMap<String, PublicKey>()
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).apply {
        timeZone = TimeZone.getTimeZone("GMT+8")
    }

    override val provider: PaymentProvider = PaymentProvider.ALIPAY

    override fun createOrder(command: CreateOrderCommand): CreateOrderResult {
        val ctx = resolveMerchant(command.merchantId)
        return when (command.channel) {
            PaymentChannel.ALIPAY_WAP,
            PaymentChannel.ALIPAY_PAGE,
            PaymentChannel.ALIPAY_APP -> createStandardOrder(command, ctx)
            PaymentChannel.ALIPAY_MINI_PROGRAM -> createMiniProgramOrder(command, ctx)
            else -> throw IllegalArgumentException("Unsupported alipay channel ${command.channel}")
        }
    }

    private fun createStandardOrder(command: CreateOrderCommand, ctx: AlipayMerchantContext): CreateOrderResult {
        val (method, productCode, channel) = when (command.channel) {
            PaymentChannel.ALIPAY_WAP -> Triple("alipay.trade.wap.pay", "QUICK_WAP_WAY", PaymentChannel.ALIPAY_WAP)
            PaymentChannel.ALIPAY_PAGE -> Triple("alipay.trade.page.pay", "FAST_INSTANT_TRADE_PAY", PaymentChannel.ALIPAY_PAGE)
            PaymentChannel.ALIPAY_APP -> Triple("alipay.trade.app.pay", "QUICK_MSECURITY_PAY", PaymentChannel.ALIPAY_APP)
            else -> throw IllegalArgumentException("Unsupported channel ${command.channel}")
        }
        val notifyUrl = command.notifyUrl ?: ctx.merchant.notifyUrl
        val returnUrl = ctx.merchant.returnUrl
        val bizContent = mutableMapOf<String, Any?>(
            "out_trade_no" to command.outTradeNo,
            "total_amount" to formatAmount(command.amount.total),
            "subject" to command.description,
            "product_code" to productCode
        )
        command.expireTime?.let { bizContent["time_expire"] = dateFmt.format(Date.from(it)) }
        val signed = buildSignedParams(ctx, method, notifyUrl, returnUrl, bizContent)
        val orderStr = signed.encoded
        val credential = if (channel == PaymentChannel.ALIPAY_APP) {
            mapOf("orderStr" to orderStr)
        } else {
            mapOf("gateway" to ctx.merchant.endpoint, "orderStr" to orderStr)
        }
        return CreateOrderResult(channel = channel, credential = credential, raw = signed.rawParams)
    }

    private fun createMiniProgramOrder(command: CreateOrderCommand, ctx: AlipayMerchantContext): CreateOrderResult {
        val buyerId = command.payer?.userId
            ?: throw IllegalArgumentException("payer.userId (Alipay user_id) is required for mini program payment")
        val bizContent = mutableMapOf<String, Any?>(
            "out_trade_no" to command.outTradeNo,
            "total_amount" to formatAmount(command.amount.total),
            "subject" to command.description,
            "buyer_id" to buyerId,
            "product_code" to "JSAPI_PAY"
        )
        command.expireTime?.let { bizContent["time_expire"] = dateFmt.format(Date.from(it)) }
        val payload = callApi(ctx, "alipay.trade.create", bizContent)
        val tradeNo = payload["trade_no"]?.asText().orEmpty()
        if (tradeNo.isBlank()) {
            throw PaymentClientException("Alipay mini program response missing trade_no")
        }
        return CreateOrderResult(
            channel = PaymentChannel.ALIPAY_MINI_PROGRAM,
            credential = mapOf("tradeNo" to tradeNo),
            raw = payload
        )
    }

    override fun queryOrder(command: QueryOrderCommand): QueryOrderResult {
        val ctx = resolveMerchant(command.merchantId)
        val bizContent = mutableMapOf<String, Any?>()
        command.outTradeNo?.let { bizContent["out_trade_no"] = it }
        command.transactionId?.let { bizContent["trade_no"] = it }
        if (bizContent.isEmpty()) throw IllegalArgumentException("Either outTradeNo or transactionId is required")
        val resp = callApi(ctx, "alipay.trade.query", bizContent)
        val tradeStatus = resp["trade_status"]?.asText()
        val status = when (tradeStatus) {
            "TRADE_SUCCESS", "TRADE_FINISHED" -> PaymentStatus.SUCCESS
            "WAIT_BUYER_PAY" -> PaymentStatus.PROCESSING
            "TRADE_CLOSED" -> PaymentStatus.CLOSED
            else -> PaymentStatus.UNKNOWN
        }
        return QueryOrderResult(
            channel = command.channelHint ?: PaymentChannel.ALIPAY_PAGE,
            status = status,
            outTradeNo = resp["out_trade_no"]?.asText() ?: command.outTradeNo.orEmpty(),
            providerTransactionId = resp["trade_no"]?.asText(),
            raw = resp
        )
    }

    override fun closeOrder(command: CloseOrderCommand): CloseOrderResult {
        val ctx = resolveMerchant(command.merchantId)
        val bizContent = mutableMapOf<String, Any?>("out_trade_no" to command.outTradeNo)
        val resp = callApi(ctx, "alipay.trade.close", bizContent)
        val accepted = resp["code"]?.asText() == "10000"
        return CloseOrderResult(
            channel = command.channelHint ?: PaymentChannel.ALIPAY_PAGE,
            outTradeNo = command.outTradeNo,
            accepted = accepted,
            raw = resp
        )
    }

    override fun refund(command: RefundCommand): RefundResult {
        val ctx = resolveMerchant(command.merchantId)
        val bizContent = mutableMapOf<String, Any?>()
        command.outTradeNo?.let { bizContent["out_trade_no"] = it }
        command.transactionId?.let { bizContent["trade_no"] = it }
        if (bizContent.isEmpty()) throw IllegalArgumentException("Either outTradeNo or transactionId is required for refund")
        bizContent["refund_amount"] = formatAmount(command.refundAmount.total)
        bizContent["refund_reason"] = command.reason
        val resp = callApi(ctx, "alipay.trade.refund", bizContent)
        val code = resp["code"]?.asText()
        val status = if (code == "10000") PaymentStatus.SUCCESS else PaymentStatus.UNKNOWN
        return RefundResult(
            channel = command.channelHint ?: PaymentChannel.ALIPAY_PAGE,
            status = status,
            outRefundNo = command.outRefundNo,
            providerRefundId = resp["trade_no"]?.asText(),
            raw = resp
        )
    }

    private fun callApi(ctx: AlipayMerchantContext, method: String, bizContent: Map<String, Any?>): com.fasterxml.jackson.databind.JsonNode {
        val signed = buildSignedParams(ctx, method, ctx.merchant.notifyUrl, ctx.merchant.returnUrl, bizContent)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val entity = HttpEntity(signed.encoded, headers)
        val resp: ResponseEntity<String> = restTemplate.postForEntity(ctx.merchant.endpoint, entity, String::class.java)
        val body = resp.body ?: throw PaymentClientException("Empty response from Alipay")
        val root = mapper.readTree(body)
        val nodeName = method.replace(".", "_") + "_response"
        val payload = root.path(nodeName)
        val code = payload.path("code").asText()
        if (code != "10000") {
            val msg = payload.path("sub_msg").asText(payload.path("msg").asText("unknown"))
            throw PaymentClientException("Alipay API error code=$code msg=$msg body=$body")
        }
        return payload
    }

    private fun buildSignedParams(
        ctx: AlipayMerchantContext,
        method: String,
        notifyUrl: String?,
        returnUrl: String?,
        bizContent: Map<String, Any?>
    ): SignedParams {
        val params = TreeMap<String, String>()
        params["app_id"] = ensure(ctx.merchant.appId, "alipay.payment.merchants[${ctx.key}].app-id")
        params["method"] = method
        params["format"] = "json"
        params["charset"] = ctx.merchant.charset
        params["sign_type"] = ctx.merchant.signType
        params["timestamp"] = dateFmt.format(Date())
        params["version"] = "1.0"
        notifyUrl?.let { params["notify_url"] = it }
        returnUrl?.let { params["return_url"] = it }
        params["biz_content"] = mapper.writeValueAsString(bizContent.filterValues { it != null })
        val signContent = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val signature = sign(ctx, signContent, ctx.merchant.signType)
        params["sign"] = signature
        val encoded = params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, ctx.merchant.charset)}" }
        return SignedParams(params, encoded)
    }

    private fun sign(ctx: AlipayMerchantContext, content: String, signType: String): String {
        val algorithm = if (signType.equals("RSA2", true)) "SHA256withRSA" else "SHA1withRSA"
        val signer = Signature.getInstance(algorithm)
        signer.initSign(resolvePrivateKey(ctx))
        signer.update(content.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(signer.sign())
    }

    private fun resolvePrivateKey(ctx: AlipayMerchantContext): PrivateKey {
        return privateKeyCache.computeIfAbsent(ctx.key) {
            val pem = when {
                !ctx.merchant.privateKey.isNullOrBlank() -> ctx.merchant.privateKey!!
                !ctx.merchant.privateKeyPath.isNullOrBlank() -> FileInputStream(ctx.merchant.privateKeyPath).use { readIns(it) }
                else -> throw IllegalArgumentException("Private key not configured for alipay merchant ${ctx.key}")
            }
            val normalized = pem
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")
            val spec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(normalized))
            KeyFactory.getInstance("RSA").generatePrivate(spec)
        }
    }

    private fun resolvePublicKey(ctx: AlipayMerchantContext): PublicKey {
        return publicKeyCache.computeIfAbsent(ctx.key) {
            val pem = when {
                !ctx.merchant.alipayPublicKey.isNullOrBlank() -> ctx.merchant.alipayPublicKey!!
                !ctx.merchant.alipayPublicKeyPath.isNullOrBlank() -> FileInputStream(ctx.merchant.alipayPublicKeyPath).use { readIns(it) }
                else -> throw IllegalArgumentException("Alipay public key not configured for merchant ${ctx.key}")
            }
            val normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s+".toRegex(), "")
            val spec = X509EncodedKeySpec(Base64.getDecoder().decode(normalized))
            KeyFactory.getInstance("RSA").generatePublic(spec)
        }
    }

    private fun verifySignature(ctx: AlipayMerchantContext, params: Map<String, String>): Boolean {
        val sign = params["sign"] ?: return false
        val signType = params["sign_type"] ?: ctx.merchant.signType
        val content = params
            .filterKeys { it != "sign" && it != "sign_type" }
            .filterValues { !it.isNullOrBlank() }
            .toSortedMap()
            .entries.joinToString("&") { "${it.key}=${it.value}" }
        return try {
            val algorithm = if (signType.equals("RSA2", true)) "SHA256withRSA" else "SHA1withRSA"
            val verifier = Signature.getInstance(algorithm)
            verifier.initVerify(resolvePublicKey(ctx))
            verifier.update(content.toByteArray(StandardCharsets.UTF_8))
            verifier.verify(Base64.getDecoder().decode(sign))
        } catch (ex: Exception) {
            logger.warn("Verify alipay signature failed", ex)
            false
        }
    }

    fun parsePaymentNotification(merchantId: String?, params: Map<String, String>): AlipayNotification? {
        val ctx = resolveMerchant(merchantId)
        if (!verifySignature(ctx, params)) {
            logger.warn("Alipay notify signature invalid for merchant={}", ctx.key)
            return null
        }
        val status = mapTradeStatus(params["trade_status"])
        return AlipayNotification(
            status = status,
            outTradeNo = params["out_trade_no"].orEmpty(),
            tradeNo = params["trade_no"],
            buyerId = params["buyer_id"],
            totalAmount = params["total_amount"],
            raw = params
        )
    }

    private fun mapTradeStatus(tradeStatus: String?): PaymentStatus =
        when (tradeStatus) {
            "TRADE_SUCCESS", "TRADE_FINISHED" -> PaymentStatus.SUCCESS
            "WAIT_BUYER_PAY" -> PaymentStatus.PROCESSING
            "TRADE_CLOSED" -> PaymentStatus.CLOSED
            else -> PaymentStatus.UNKNOWN
        }

    private fun formatAmount(amountInFen: Long): String =
        BigDecimal(amountInFen).divide(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP).toPlainString()

    private fun ensure(value: String?, field: String): String =
        value?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException("$field must be configured")

    private fun resolveMerchant(merchantId: String?): AlipayMerchantContext {
        val resolved = merchantId ?: properties.defaultMerchant
        require(!resolved.isNullOrBlank()) { "Unable to resolve alipay merchantId, configure alipay.payment.default-merchant" }
        val merchant = properties.merchants[resolved]
            ?: throw IllegalArgumentException("Cannot find alipay merchant config for id=$resolved")
        return AlipayMerchantContext(resolved, merchant)
    }
}

data class AlipayMerchantContext(val key: String, val merchant: AlipayMerchantProperties)

data class AlipayNotification(
    val status: PaymentStatus,
    val outTradeNo: String,
    val tradeNo: String?,
    val buyerId: String?,
    val totalAmount: String?,
    val raw: Map<String, String>
)

private data class SignedParams(val rawParams: Map<String, String>, val encoded: String)
