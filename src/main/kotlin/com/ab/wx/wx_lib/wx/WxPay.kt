package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.dto.pay.*
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.fn.aes.WxPayAes
import com.ab.wx.wx_lib.vo.pay.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.web.client.toEntity
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.net.URL
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


class WxPay(wxConfigProperties: WxConfigProperties) {
    private val mchId = wxConfigProperties.pay?.mchid
    private val notifyUrl = wxConfigProperties.pay?.notifyUrl
    private val refundsNotifyUrl = wxConfigProperties.pay?.refundsNotifyUrl
    private val transCallbackUrl = wxConfigProperties.pay?.transCallbackUrl

    //    private val v3key = wxConfigProperties.pay?.v3key
    private val keyPath = wxConfigProperties.pay?.keyPath
    private val serialNo = wxConfigProperties.pay?.serialNo
    private val publicKeyPath = wxConfigProperties.pay?.publicKeyPath

    private val v3Key = wxConfigProperties.pay?.v3key

    private val SCHEMA = "WECHATPAY2-SHA256-RSA2048"
    private val SIGN_METHOD = "SHA256withRSA"
    private val UTF8 = "UTF-8"

    private val appId = wxConfigProperties.appId

    private val miniAppId = wxConfigProperties.miniAppId

    //    private val restTemplate = getRestTemplate()
    private val restClient = getRestClient()
    private val mapper = getMapper()

    private var x509Certificate: X509Certificate? = null
    private var payCert: PayCert? = null

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


    private val refuseUrl = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds"

    /**
     * 商户转账
     */
    private val transferUrl = "https://api.mch.weixin.qq.com/v3/transfer/batches"

    /**
     * 添加分账接收方
     */
    private val addReceiverUrl = "https://api.mch.weixin.qq.com/v3/profitsharing/receivers/add"

    /**
     * 删除分账接收方
     */
    private val deleteReceiverUrl = "https://api.mch.weixin.qq.com/profitsharing/receivers/delete"

    /**
     * 请求分账
     */
    private val requestTransferUrl = "https://api.mch.weixin.qq.com/v3/profitsharing/orders"

    /**
     * 解冻资金
     */
    private val unfreezeUrl = "https://api.mch.weixin.qq.com/v3/profitsharing/orders/unfreeze"

    /**
     *创建投诉通知回调地址
     */
    private val complaintNotifyUrl = "https://api.mch.weixin.qq.com/v3/merchant-service/complaint-notifications"

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


    fun transfer(dto: TransPayDto): TransferVo? {
        v3Key?.let { v3 ->
            val json = mapper.writeValueAsString(
                dto.copy(
                    appid = miniAppId, transfer_detail_list = dto.transfer_detail_list.map {
                        it.copy(
                            user_name = encodeSensitive(
                                it.user_name, autoGenCert(apiV3Key = v3)
                            )
                        )
                    }, notify_url = transCallbackUrl
                )
            )
            val header = getPayHeaders(genToken("POST", transferUrl, json))
            logger("pay:$payCert")
            header.add("Wechatpay-Serial", payCert?.serial_no)
            val entity = HttpEntity(json, header)
//            val res = restTemplate.postForObject(transferUrl, entity, TransferVo::class.java)
            val res = restClient.post().uri(transferUrl).headers {
                it.addAll(header)
            }.contentType(MediaType.APPLICATION_JSON).body(json).retrieve().toEntity<TransferVo>().body
            logger("transer res :$res")
            return res
        }
        return null
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
        logger("keyPath:${keyPath.isNullOrBlank()}")
        if (keyPath.isNullOrBlank()) {
            logger("getLastCert:${getLastCert()}")
        } else {
            FileInputStream(keyPath).use {
                result = readIns(it)
            }
        }
        logger("genPrivateKeyWithPath:$result")
        return result
    }

    private fun genPublicKeyWithPath(): PublicKey? {
        publicKeyPath?.let {
            val cf = CertificateFactory.getInstance("X.509")
            val cert = cf.generateCertificate(FileInputStream(publicKeyPath))
            return cert.publicKey
        }
        return null
    }


    //    private fun genFirstToken(method: String, url: String, body: String): String {
//        val noticeStr = create_pay_nonce()
//        val time = create_timestamp()
//        val processUrl = URL(url).path
//        val message = genPaySign(method, processUrl, time, noticeStr, body)
//        logger.info("message:$message")
//        val signature = sign(message.toByteArray(charset(UTF8)))
//        return " mchid=\"$mchId\",nonce_str=\"$noticeStr\",timestamp=\"$time\",serial_no=\"$serialNo\",signature=\"$signature\""
//    }
//
    fun genToken(method: String, url: String, body: String, initFlag: Boolean = false): String {
        val noticeStr = create_pay_nonce()
        val time = create_timestamp()
        val processUrl = URL(url).path
        val message = genPaySign(method, processUrl, time, noticeStr, body)
//        logger("message:$message")
        val signature = sign(message.toByteArray(charset(UTF8)), initFlag)
//        val signature = autoSelectSign(message.toByteArray(charset(UTF8)))
//        val signature = signWithAutoKey(message.toByteArray(charset(UTF8)))
        val res =
            "$SCHEMA mchid=\"$mchId\",nonce_str=\"$noticeStr\",timestamp=\"$time\",serial_no=\"$serialNo\",signature=\"$signature\""
        logger("token res:$res")
        return res
    }



    private fun sign(message: ByteArray, initFlag: Boolean = false): String? {
        val s = Signature.getInstance(SIGN_METHOD)
        s.initSign(loadPrivateKeyFromString(genPrivateKeyWithPath()))
//            s.initVerify(x509Certificate)
        s.update(message)
        return Base64.getEncoder().encodeToString(s.sign())
    }


    fun genSimplePay(dto: SimplePayDto, method: String): JsApiPayRes? {
        return payFn(dto, method, appId)
    }

    fun genMiniAppPay(dto: SimplePayDto, method: String): JsApiPayRes? {
        return payFn(dto, method, miniAppId)
    }

    private fun payFn(dto: SimplePayDto, method: String, appId: String): JsApiPayRes? {
        mchId?.let {
            val payDto = JsApiPayDto(
                appid = appId,
                mchid = mchId,
                description = dto.description,
                out_trade_no = dto.orderNo,
                notify_url = dto.notifyUrl.ifBlank { "$notifyUrl" },
                amount = JsApiPayAmountDto(total = dto.amount),
                payer = JsApiPayerDto(dto.payOpenid),
                settle_info = if (dto.profit_sharing) SettleInfoDto(true) else null
            )
            return genJsApiPay(payDto, method, dto.orderNo, appId)
        }
        return null
    }

    private fun genJsApiPay(dto: JsApiPayDto, method: String, orderNo: String, appId: String): JsApiPayRes? {
        val json = mapper.writeValueAsString(dto)
        val header = getPayHeaders(genToken(method, jsApiPayUrl, json))
        val entity = HttpEntity(json, header)
//        val res = restTemplate.exchange(jsApiPayUrl, HttpMethod.POST, entity, String::class.java).body
//        val
//        r mapper.readValue(res, JsApiPayVo::class.java)
//        val res = restTemplate.postForObject(jsApiPayUrl, entity, HashMap::class.java)
        val res =
            restClient.post().uri(jsApiPayUrl).headers { it.addAll(header) }.contentType(MediaType.APPLICATION_JSON)
                .body(json).retrieve().toEntity(HashMap::class.java).body
        logger("调用微信支付:$res")
        res?.let {
            return genJsSign("${it["prepay_id"]}", orderNo, appId)
        }
        return null
    }

    fun verity(body: String, sign: String, serial: String): Boolean {
        x509Certificate?.let {
            val no = it.serialNumber.toString(16).uppercase()
            return if (no == serial) {
                val rsa = Signature.getInstance(SIGN_METHOD)
                rsa.initVerify(it.publicKey)
                rsa.update(body.toByteArray(charset(UTF8)))
                rsa.verify(Base64.getDecoder().decode(sign))
            } else false
        }
        return false
    }

    /**
     * 生成前端的sign
     */
    private fun genJsSign(prepayId: String, orderNo: String, appId: String): JsApiPayRes {
        val time = create_timestamp()
        val notifyCode = create_pay_nonce()
        val signType = sign(genPaySign(appId, time, notifyCode, "prepay_id=$prepayId").toByteArray())
        return JsApiPayRes(
            prepayId = prepayId, timestamp = time, nonceStr = notifyCode, paySign = signType, orderId = orderNo
        )
    }

    private fun decodeCallback(request: HttpServletRequest, apiV3Key: String): String? {
        val timestamp = request.getHeader("wechatpay-timestamp")
        val nonce = request.getHeader("wechatpay-nonce")
        val signature = request.getHeader("wechatpay-signature")
        val serial = request.getHeader("Wechatpay-Serial")

        val body = readIns(request.inputStream)
        val wxPayRes = getMapper().readValue(body, H5PayVo::class.java)
        return WxPayAes.decryptToString(
            wxPayRes.resource.associated_data, wxPayRes.resource.nonce, wxPayRes.resource.ciphertext, apiV3Key
        )
    }

    /**
     * 支付回调
     */
    fun callbackFn(request: HttpServletRequest, apiV3Key: String): H5PayDecodeVo? {
        val decodeStr = decodeCallback(request, apiV3Key)
        return getMapper().readValue(decodeStr, H5PayDecodeVo::class.java)
    }

    /**
     * 退款回调
     */
    fun refundsCallbackFn(request: HttpServletRequest, apiV3Key: String): H5RefundsDecodeVo? {
        val decodeStr = decodeCallback(request, apiV3Key)
        return getMapper().readValue(decodeStr, H5RefundsDecodeVo::class.java)
    }

    /**
     * 微信转账零钱回调
     */
    fun transCallbackFn(request: HttpServletRequest, apiV3Key: String): TransferCallbackVo? {
        val decodeStr = decodeCallback(request, apiV3Key)
        return getMapper().readValue(decodeStr, TransferCallbackVo::class.java)
    }

    /**
     * 获取证书
     */
    private fun genCert(): PayCertResVo? {
        val header = getPayHeaders(genToken("GET", getCertsUrl, ""))
        logger("header:$header")
        val entity = HttpEntity("", header)
//        return restTemplate.exchange(getCertsUrl, HttpMethod.GET, entity, PayCertResVo::class.java).body
        return restClient.get().uri(getCertsUrl).headers { h ->
            h.addAll(header)
        }.retrieve().toEntity(PayCertResVo::class.java).body
    }

    fun getLastCert(): PayCert? {
        genCert()?.let {
            return it.data.maxByOrNull { it.expire_time }!!
        }
        return null
    }

    fun autoGenCert(apiV3Key: String): X509Certificate? {
        val lastCert = getLastCert()
        payCert = lastCert
        lastCert?.let {
            val encryptCert = it.encrypt_certificate
            val cf = CertificateFactory.getInstance("X509")
            val res = WxPayAes.decryptToString(
                encryptCert.associated_data, encryptCert.nonce, encryptCert.ciphertext, apiV3Key
            )
            val cert: X509Certificate =
                cf.generateCertificate(ByteArrayInputStream(res?.toByteArray(charset(UTF8)))) as X509Certificate
            cert.checkValidity()

            x509Certificate = cert
            return cert
        }
        return null
    }

    /**
     * 简易退款
     */
    fun simpleRefunds(dto: SimpleRefundsDto): String? {
        return refunds(
            RefundPayDto(
                out_refund_no = dto.refundsOrderId, out_trade_no = dto.orderId, amount = RefundsAmount(
                    refund = dto.refundsMoney, total = dto.totalMoney
                ), notify_url = refundsNotifyUrl
            )
        )
    }

    private fun refunds(refundPayDto: RefundPayDto): String? {
        val json = mapper.writeValueAsString(refundPayDto)
        val header = getPayHeaders(genToken("POST", refuseUrl, json))
        return restClient.post().uri(refuseUrl).headers {
            it.addAll(header)
        }.body(json).retrieve().toEntity<String>().body
    }

    fun refundsWithPromotion(dto: RefundsWithPromotionDto): String? {
        return refunds(
            RefundPayDto(
                out_refund_no = dto.refundsOrderId, out_trade_no = dto.orderId,
                amount = RefundsAmount(
                    refund = dto.refundsMoney, total = dto.totalMoney,
                ),
                notify_url = refundsNotifyUrl,
            )
        )
    }

    /**
     * 加密敏感信息
     */
    fun encodeSensitive(msg: String?, certificate: X509Certificate?): String? {
        val instance = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
        instance.init(Cipher.ENCRYPT_MODE, certificate?.publicKey)
        val message = msg?.toByteArray()
        return Base64.getEncoder().encodeToString(instance.doFinal(message))
    }


    /**
     * 添加分账接收方
     */
    fun addReceiver(addReceiverDto: AddReceiverDto): AddReceiverVo? {
        v3Key?.let {
            val json = mapper.writeValueAsString(
                addReceiverDto.copy(
                    name = encodeSensitive(
                        addReceiverDto.name, autoGenCert(apiV3Key = v3Key)
                    )
                )
            )
            val header = getPayHeaders(genToken("POST", addReceiverUrl, json))
            header.add("Wechatpay-Serial", payCert?.serial_no)
            return restClient.post().uri(addReceiverUrl).headers {
                it.addAll(header)
            }.body(json).retrieve().toEntity<AddReceiverVo>().body
        }
        return null
    }

    fun delReceiver(delReceiverDto: DelReceiverDto): DelReceiverVo? {
        val json = mapper.writeValueAsString(delReceiverDto)
        val header = getPayHeaders(genToken("POST", deleteReceiverUrl, json))
        return restClient.post().uri(deleteReceiverUrl).headers {
            it.addAll(header)
        }.body(json).retrieve().toEntity(DelReceiverVo::class.java).body
    }

    fun requestTransfer(requestOrderDto: RequestOrderDto): RequestOrderVo? {
        v3Key?.let {
            val json = mapper.writeValueAsString(
                requestOrderDto.copy(
                    receivers = requestOrderDto.receivers.map {
                        it.copy(
                            name = encodeSensitive(
                                it.name, autoGenCert(apiV3Key = v3Key)
                            )
                        )
                    })
            )
            val header = getPayHeaders(genToken("POST", requestTransferUrl, json))
            header.add("Wechatpay-Serial", payCert?.serial_no)
            return restClient.post().uri(requestTransferUrl).headers {
                it.addAll(header)
            }.body(json).retrieve().toEntity(RequestOrderVo::class.java).body
        }
        return null
    }

    fun unfreeze(unfreezeDto: UnfreezeDto): UnfreezeVo? {
        val json = mapper.writeValueAsString(unfreezeDto)
        val header = getPayHeaders(genToken("POST", unfreezeUrl, json))
        header.add("Wechatpay-Serial", payCert?.serial_no)
        return restClient.post().uri(unfreezeUrl).headers {
            it.addAll(header)
        }.body(json).retrieve().toEntity(UnfreezeVo::class.java).body
    }

    fun complaintNotify(dto: ComplaintNotifyDto): ComplaintNotifyVo? {
        val json = mapper.writeValueAsString(dto)
        val header = getPayHeaders(genToken("POST", complaintNotifyUrl, json))
        return restClient.post().uri(complaintNotifyUrl).headers {
            it.addAll(header)
        }.body(json).retrieve().toEntity(ComplaintNotifyVo::class.java).body
    }

    /**
     * 投诉回调
     */
    fun complaintNotifyCallback(request: HttpServletRequest): String? {
        val body = readIns(request.inputStream)
        val res = getMapper().readValue(body, ComplainNotifyTextVo::class.java)
        v3Key?.let {
            return WxPayAes.decryptToString(
                res.resource?.associated_data, res.resource?.nonce, res.resource?.ciphertext, v3Key
            )
        }
        return null
    }
}
