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
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
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
    private val publicKeyNo = wxConfigProperties.pay?.publicKeyNo

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
     * 请求分账退回
     */
    private val requestTransferReturnUrl = "https://api.mch.weixin.qq.com/v3/profitsharing/return-orders"

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
                                it.user_name
                            )
                        )
                    }, notify_url = transCallbackUrl
                )
            )
            val header = getPayHeaders(genToken("POST", transferUrl, json))
            logger("pay:$payCert")
            header.add("Wechatpay-Serial", if (publicKeyNo.isNullOrBlank()) payCert?.serial_no else publicKeyNo)
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
            val keyContent = File(publicKeyPath).readText(Charsets.UTF_8)
            val publicKeyPEM =
                keyContent.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
                    .replace("\\s".toRegex(), "")

            val decodedKey = Base64.getDecoder().decode(publicKeyPEM)
            val keySpec = X509EncodedKeySpec(decodedKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            return keyFactory.generatePublic(keySpec)
        }
        return null
    }


    fun genToken(method: String, url: String, body: String, initFlag: Boolean = false): String {
        val noticeStr = create_pay_nonce()
        val time = create_timestamp()
        val processUrl = URL(url).path
        val message = genPaySign(method, processUrl, time, noticeStr, body)
        val signature = sign(message.toByteArray(charset(UTF8)), initFlag)
        val res =
            "$SCHEMA mchid=\"$mchId\",nonce_str=\"$noticeStr\",timestamp=\"$time\",serial_no=\"$serialNo\",signature=\"$signature\""
        logger("token res:$res")
        return res
    }


    private fun sign(message: ByteArray, initFlag: Boolean = false): String? {
        val s = Signature.getInstance(SIGN_METHOD)
        s.initSign(loadPrivateKeyFromString(genPrivateKeyWithPath()))
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
     * 使用公钥验签
     */
    fun verifyByPublicKey(str: String, signature: String, serial: String): Boolean {
        logger("verifyByPublicKey:$str signature:$signature serial:$serial")
        logger("publicKeyNo:$publicKeyNo")
        if (serial !== publicKeyNo) return false
        val rsa = Signature.getInstance(SIGN_METHOD)
        rsa.initVerify(genPublicKeyWithPath())
        rsa.update(str.toByteArray(charset(UTF8)))
        return rsa.verify(Base64.getDecoder().decode(signature))
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
        logger("serial:$serial")

        val body = readIns(request.inputStream)
        logger("verify:${verifyByPublicKey(genPaySign(timestamp, nonce, body), signature, serial)}")
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

    @Deprecated("迁移到微信公钥")
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
    fun encodeSensitive(msg: String?): String? {
        val instance = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
        v3Key?.let {
            val key = if (publicKeyNo.isNullOrBlank()) autoGenCert(v3Key)?.publicKey else genPublicKeyWithPath()
            instance.init(Cipher.ENCRYPT_MODE, key)
            val message = msg?.toByteArray()
            return Base64.getEncoder().encodeToString(instance.doFinal(message))
        }
        return null
    }


    /**
     * 添加分账接收方
     */
    fun addReceiver(addReceiverDto: AddReceiverDto): AddReceiverVo? {
        v3Key?.let {
            val json = mapper.writeValueAsString(
                addReceiverDto.copy(
                    name = encodeSensitive(
                        addReceiverDto.name
                    )
                )
            )
            val header = getPayHeaders(genToken("POST", addReceiverUrl, json))
            header.add("Wechatpay-Serial", if (publicKeyNo.isNullOrBlank()) payCert?.serial_no else publicKeyNo)
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
                                it.name
                            )
                        )
                    })
            )
            val header = getPayHeaders(genToken("POST", requestTransferUrl, json))
            header.add("Wechatpay-Serial", if (publicKeyNo.isNullOrBlank()) payCert?.serial_no else publicKeyNo)
            return restClient.post().uri(requestTransferUrl).headers {
                it.addAll(header)
            }.body(json).retrieve().toEntity(RequestOrderVo::class.java).body
        }
        return null
    }

    fun unfreeze(unfreezeDto: UnfreezeDto): UnfreezeVo? {
        val json = mapper.writeValueAsString(unfreezeDto)
        val header = getPayHeaders(genToken("POST", unfreezeUrl, json))
        header.add("Wechatpay-Serial", if (publicKeyNo.isNullOrBlank()) payCert?.serial_no else publicKeyNo)
        return restClient.post().uri(unfreezeUrl).headers {
            it.addAll(header)
        }.body(json).retrieve().toEntity(UnfreezeVo::class.java).body
    }

    /**
     * 请求分账结果
     */
    fun fetchTransResult(dto: FetchTransResultDto): FetchTransResultVo? {
        val url =
            "https://api.mch.weixin.qq.com/v3/profitsharing/orders/${dto.out_order_no}?transaction_id=${dto.transaction_id}"
        val header = getPayHeaders(genToken("POST", url, ""))
        header.add("Wechatpay-Serial", if (publicKeyNo.isNullOrBlank()) payCert?.serial_no else publicKeyNo)
        return restClient.get().uri(url).headers {
            it.addAll(header)
        }.retrieve().toEntity(FetchTransResultVo::class.java).body
    }

    /**
     * 分流后申请资金退回
     */
    fun transRefunds(dto: TransReturnDto): TransReturnVo? {
        val json = mapper.writeValueAsString(dto)
        val header = getPayHeaders(genToken("POST", requestTransferReturnUrl, json))
        header.add("Wechatpay-Serial", if (publicKeyNo.isNullOrBlank()) payCert?.serial_no else publicKeyNo)
        return restClient.post().uri(requestTransferReturnUrl).headers {
            it.addAll(header)
        }.body(json).retrieve().toEntity(TransReturnVo::class.java).body
    }

    fun complaintNotify(dto: ComplaintNotifyDto): ComplaintNotifyVo? {
        val json = mapper.writeValueAsString(dto)
        val header = getPayHeaders(genToken("POST", complaintNotifyUrl, json))
        return restClient.post().uri(complaintNotifyUrl).headers {
            it.addAll(header)
        }.body(json).retrieve().toEntity(ComplaintNotifyVo::class.java).body
    }

    fun getComplaintNotify(): ComplaintNotifyVo? {
        val header = getPayHeaders(genToken("GET", complaintNotifyUrl, body = ""))
        val res = restClient.get().uri(complaintNotifyUrl).headers {
            it.addAll(header)
        }.retrieve().toEntity(ComplaintNotifyVo::class.java).body
        return res
    }
    fun putComplaintNotify(dto: ComplaintNotifyDto): ComplaintNotifyVo? {
        val json = mapper.writeValueAsString(dto)
        val header = getPayHeaders(genToken("PUT", complaintNotifyUrl, json))
        return restClient.put().uri(complaintNotifyUrl).headers {
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
