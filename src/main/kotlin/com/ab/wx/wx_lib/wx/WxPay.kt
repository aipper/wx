package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.dto.pay.JsApiPayAmountDto
import com.ab.wx.wx_lib.dto.pay.JsApiPayDto
import com.ab.wx.wx_lib.dto.pay.JsApiPayerDto
import com.ab.wx.wx_lib.dto.pay.SimplePayDto
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.fn.aes.WxPayAes
import com.ab.wx.wx_lib.vo.pay.JsApiPayRes
import com.ab.wx.wx_lib.vo.pay.JsApiPayVo
import com.ab.wx.wx_lib.vo.pay.PayCert
import com.ab.wx.wx_lib.vo.pay.PayCertResVo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.net.URL
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


class WxPay(wxConfigProperties: WxConfigProperties) {
    private val logger = LoggerFactory.getLogger(WxPay::class.java)
    private val mchId = wxConfigProperties.pay?.mchid
    private val notifyUrl = wxConfigProperties.pay?.notifyUrl

    //    private val v3key = wxConfigProperties.pay?.v3key
    private val keyPath = wxConfigProperties.pay?.keyPath
    private val serialNo = wxConfigProperties.pay?.serialNo

    private val SCHEMA = "WECHATPAY2-SHA256-RSA2048"
    private val SIGN_METHOD = "SHA256withRSA"
    private val UTF8 = "UTF-8"

    private val appId = wxConfigProperties.appId

    private val restTemplate = getRestTemplate()
    private val mapper = getMapper()

    /**
     * jsapi支付接口
     */
    private val jsApiPayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi"


    /**
     * h5支付接口
     */
    private val h5PayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5"

    /**
     * 获取平台证书
     */
    private val getCertsUrl = "https://api.mch.weixin.qq.com/v3/certificates"


//    fun genPaySign(method: String, url: String, time: String, nonceStr: String, content: String): String {
//        return """
//            $method
//            $url
//            $time
//            $nonceStr
//            $content
//
//        """.trimIndent()
//    }


    private fun loadPrivateKeyFromString(keyString: String): PrivateKey? {
        return try {
            val tmp = keyString.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")
            KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(tmp)))
        } catch (e: NoSuchAlgorithmException) {
            throw UnsupportedOperationException(e)
        } catch (e: InvalidKeySpecException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun genPrivateKeyWithPath(): String {
        var result: String = ""
        if (keyPath != null) {
            FileInputStream(keyPath).use {
                result = readIns(it)
            }
        }
        return result
    }


    private fun genPrivateKey(key: String) {

    }

    fun genToken(method: String, url: String, body: String): String {
        val noticeStr = create_pay_nonce()
        val time = create_timestamp()
        val processUrl = URL(url).path
        val message = genPaySign(method, processUrl, time, noticeStr, body)
        logger.info("message:$message")
        val signature = sign(message.toByteArray(charset(UTF8)))
        return " mchid=\"$mchId\",nonce_str=\"$noticeStr\",timestamp=\"$time\",serial_no=\"$serialNo\",signature=\"$signature\""
    }

    private fun sign(message: ByteArray): String? {
        val s = Signature.getInstance(SIGN_METHOD)
        s.initSign(loadPrivateKeyFromString(genPrivateKeyWithPath()))
        s.update(message)
        return Base64.getEncoder().encodeToString(s.sign())
    }

    fun genSimplePay(dto: SimplePayDto, method: String): JsApiPayRes? {
        mchId?.let {
            val payDto = JsApiPayDto(
                appid = appId,
                mchid = mchId,
                description = dto.description,
                out_trade_no = dto.orderNo,
                notify_url = dto.notifyUrl.ifBlank { "$notifyUrl" },
                amount = JsApiPayAmountDto(total = dto.amount),
                payer = JsApiPayerDto(dto.payOpenid)
            )
            return genJsApiPay(payDto, method, dto.orderNo)
        }
        return null
    }

    private fun genJsApiPay(dto: JsApiPayDto, method: String, orderNo: String): JsApiPayRes? {
        val json = mapper.writeValueAsString(dto)
        val header = getPayHeaders(SCHEMA + genToken(method, jsApiPayUrl, json))
        val entity = HttpEntity(json, header)
//        val res = restTemplate.exchange(jsApiPayUrl, HttpMethod.POST, entity, String::class.java).body
//        val
//        r mapper.readValue(res, JsApiPayVo::class.java)
        val res = restTemplate.postForObject(jsApiPayUrl, entity, JsApiPayVo::class.java)
        logger.info("调用微信支付:$res")
        res?.let {
            return genJsSign(it.prepay_id, orderNo)
        }
        return null
    }

    fun verity(body: String, cert: X509Certificate, sign: String, serial: String): Boolean {
        val no = cert.serialNumber.toString(16).uppercase()
        return if (no == serial) {
            val rsa = Signature.getInstance(SIGN_METHOD)
            rsa.initVerify(cert.publicKey)
            rsa.update(body.toByteArray(charset(UTF8)))
            rsa.verify(Base64.getDecoder().decode(sign))
        } else false
    }

    /**
     * 生成前端的sign
     */
    private fun genJsSign(prepayId: String, orderNo: String): JsApiPayRes {
        val time = create_timestamp()
        val notifyCode = create_pay_nonce()
        val signType = sign(genPaySign(appId, time, notifyCode, "prepay_id=$prepayId").toByteArray())
        return JsApiPayRes(
            prepayId = prepayId, timestamp = time, nonceStr = notifyCode, paySign = signType, orderId = orderNo
        )
    }

    //    fun callbackFn() {
//        val timestamp = request.getHeader("wechatpay-timestamp")
//        val nonce = request.getHeader("wechatpay-nonce")
//        val signature = request.getHeader("wechatpay-signature")
//        val serial = request.getHeader("Wechatpay-Serial")
//
//        val body = readIns(request.inputStream)
//
//
//    }
    /**
     * 获取证书
     */
    private fun genCert(): PayCertResVo? {
        val header = getPayHeaders(SCHEMA + genToken("GET", getCertsUrl, ""))
        logger.info("header:$header")
        val entity = HttpEntity("", header)
        return restTemplate.exchange(getCertsUrl, HttpMethod.GET, entity, PayCertResVo::class.java).body
    }

    fun getLastCert(): PayCert? {
        genCert()?.let {
            return it.data.maxByOrNull { it.expire_time }!!
        }
        return null
    }

    fun autoGenCert(apiV3Key: String): X509Certificate? {
        val lastCert = getLastCert()
        lastCert?.let {
            val encryptCert = it.encrypt_certificate
            val cf = CertificateFactory.getInstance("X509")
            val res = WxPayAes.decryptToString(
                encryptCert.associated_data.toByteArray(charset(UTF8)),
                encryptCert.nonce.toByteArray(charset(UTF8)),
                encryptCert.ciphertext,
                apiV3Key.toByteArray(charset(UTF8))
            )
            val cert: X509Certificate =
                cf.generateCertificate(ByteArrayInputStream(res?.toByteArray(charset(UTF8)))) as X509Certificate
            cert.checkValidity()
            return cert
        }
        return null
    }


}