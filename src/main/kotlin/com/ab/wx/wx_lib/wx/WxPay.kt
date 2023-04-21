package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.dto.pay.JsApiPayAmountDto
import com.ab.wx.wx_lib.dto.pay.JsApiPayDto
import com.ab.wx.wx_lib.dto.pay.JsApiPayerDto
import com.ab.wx.wx_lib.dto.pay.SimplePayDto
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.vo.pay.JsApiPayVo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.stream.Collectors


class WxPay(wxConfigProperties: WxConfigProperties) {
    private val logger = LoggerFactory.getLogger(WxPay::class.java)
    private val mchId = wxConfigProperties.pay?.mchid
    private val notifyUrl = wxConfigProperties.pay?.notifyUrl
    private val v3key = wxConfigProperties.pay?.v3key
    private val keyPath = wxConfigProperties.pay?.keyPath
    private val serialNo = wxConfigProperties.pay?.serialNo

    private val SCHEMA = "WECHATPAY2-SHA256-RSA2048"
    private val SIGN_METHOD = "SHA256withRSA"
    private val UTF8 = "UTF-8"

    private val appId = wxConfigProperties.appId

    private val restTemplate = getRestTemplate()
    private val mapper = getMapper()

    private val jsApiPayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi"


    private val h5PayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5"

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

    private fun genPaySign(vararg components: String): String {
        return Arrays.stream(components).collect(Collectors.joining("\n", "", "\n"))
    }

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

    private fun readIns(input: InputStream): String {
        InputStreamReader(input).use {
            BufferedReader(it).use {
                val stringBuilder = StringBuilder()
                var line: String? = it.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = it.readLine()
                }
                return stringBuilder.toString()
            }
        }
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

    fun genSimplePay(dto: SimplePayDto, method: String): JsApiPayVo? {
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
            return genJsApiPay(payDto, method)
        }
        return null
    }

    private fun genJsApiPay(dto: JsApiPayDto, method: String): JsApiPayVo? {
        val json = mapper.writeValueAsString(dto)
        val header = getPayHeaders(SCHEMA + genToken(method, jsApiPayUrl, json))
        val entity = HttpEntity(json, header)
        val res = restTemplate.exchange(jsApiPayUrl, HttpMethod.POST, entity, String::class.java).body
        return mapper.readValue(res, JsApiPayVo::class.java)
    }

    public fun verity(body: String, cert: X509Certificate, sign: String, serial: String): Boolean {
        val no = cert.serialNumber.toString(16).uppercase()
        if (no == serial) {
            val rsa = Signature.getInstance(SIGN_METHOD)
            rsa.initVerify(cert.publicKey)
            rsa.update(body.toByteArray(charset(UTF8)))
            return rsa.verify(Base64.getDecoder().decode(sign))
        } else return false
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
}