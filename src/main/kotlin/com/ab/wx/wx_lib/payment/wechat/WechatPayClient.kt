package com.ab.wx.wx_lib.payment.wechat

import com.ab.wx.wx_lib.fn.create_pay_nonce
import com.ab.wx.wx_lib.fn.create_timestamp
import com.ab.wx.wx_lib.fn.genPaySign
import com.ab.wx.wx_lib.fn.getMapper
import com.ab.wx.wx_lib.fn.getPayHeaders
import com.ab.wx.wx_lib.fn.getRestTemplate
import com.ab.wx.wx_lib.fn.readIns
import com.ab.wx.wx_lib.fn.aes.WxPayAes
import com.ab.wx.wx_lib.payment.api.CloseOrderCommand
import com.ab.wx.wx_lib.payment.api.CloseOrderResult
import com.ab.wx.wx_lib.payment.api.CreateOrderCommand
import com.ab.wx.wx_lib.payment.api.CreateOrderResult
import com.ab.wx.wx_lib.payment.api.GoodsDetail
import com.ab.wx.wx_lib.payment.api.Money
import com.ab.wx.wx_lib.payment.api.PaymentChannel
import com.ab.wx.wx_lib.payment.api.PaymentClient
import com.ab.wx.wx_lib.payment.api.PaymentClientException
import com.ab.wx.wx_lib.payment.api.PaymentProvider
import com.ab.wx.wx_lib.payment.api.PaymentScene
import com.ab.wx.wx_lib.payment.api.PaymentStatus
import com.ab.wx.wx_lib.payment.api.QueryOrderCommand
import com.ab.wx.wx_lib.payment.api.QueryOrderResult
import com.ab.wx.wx_lib.payment.api.RefundCommand
import com.ab.wx.wx_lib.payment.api.RefundResult
import com.ab.wx.wx_lib.payment.config.WxMerchantMode
import com.ab.wx.wx_lib.payment.config.WxMerchantProperties
import com.ab.wx.wx_lib.payment.config.WxPaymentProperties
import com.ab.wx.wx_lib.payment.config.WxSubMerchantProperties
import com.ab.wx.wx_lib.vo.pay.H5PayDecodeVo
import com.ab.wx.wx_lib.vo.pay.H5PrepayVo
import com.ab.wx.wx_lib.vo.pay.H5RefundsDecodeVo
import com.ab.wx.wx_lib.vo.pay.JsApiPayVo
import com.ab.wx.wx_lib.vo.pay.PayCertResVo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

class WechatPayClient(
    private val properties: WxPaymentProperties
) : PaymentClient {

    private val logger = LoggerFactory.getLogger(WechatPayClient::class.java)
    private val mapper = getMapper()
    private val restTemplate: RestTemplate = getRestTemplate()
    private val privateKeyCache = ConcurrentHashMap<String, PrivateKey>()
    private val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.ofHours(8))
    private val platformCertCache = ConcurrentHashMap<String, java.security.cert.X509Certificate>()
    private val lastCertRefresh = ConcurrentHashMap<String, Instant>()

    override val provider: PaymentProvider = PaymentProvider.WECHAT

    override fun createOrder(command: CreateOrderCommand): CreateOrderResult {
        val ctx = resolveMerchant(command.merchantId, command.subMerchantId)
        return when (command.channel) {
            PaymentChannel.WECHAT_JSAPI,
            PaymentChannel.WECHAT_MINI_APP -> createJsapiOrder(command, ctx)
            PaymentChannel.WECHAT_H5 -> createH5Order(command, ctx)
            PaymentChannel.WECHAT_NATIVE -> createNativeOrder(command, ctx)
            PaymentChannel.WECHAT_APP -> createAppOrder(command, ctx)
            else -> throw IllegalArgumentException("Unsupported channel ${command.channel} for WeChat createOrder")
        }
    }

    override fun queryOrder(command: QueryOrderCommand): QueryOrderResult {
        val ctx = resolveMerchant(command.merchantId, command.subMerchantId)
        val outTradeNo = command.outTradeNo
        val transactionId = command.transactionId
        require(!outTradeNo.isNullOrBlank() || !transactionId.isNullOrBlank()) { "Either outTradeNo or transactionId must be provided" }
        val path = if (!outTradeNo.isNullOrBlank()) {
            buildQueryOutTradeNoPath(ctx, outTradeNo)
        } else {
            buildQueryTransactionPath(ctx, transactionId!!)
        }
        val response = execute(ctx, HttpMethod.GET, path, null)
        val body = parseBody(response, H5PayDecodeVo::class.java)
        val status = mapTradeState(body?.trade_state)
        val channel = command.channelHint ?: PaymentChannel.WECHAT_MINI_APP
        return QueryOrderResult(
            channel = channel,
            status = status,
            outTradeNo = body?.out_trade_no ?: outTradeNo.orEmpty(),
            providerTransactionId = body?.transaction_id,
            successTime = body?.success_time?.let { runCatching { Instant.parse(it) }.getOrNull() },
            raw = body
        )
    }

    override fun closeOrder(command: CloseOrderCommand): CloseOrderResult {
        val ctx = resolveMerchant(command.merchantId, command.subMerchantId)
        val path = buildClosePath(ctx, command.outTradeNo)
        val payload = buildClosePayload(ctx)
        val response = execute(ctx, HttpMethod.POST, path, payload)
        val channel = command.channelHint ?: PaymentChannel.WECHAT_MINI_APP
        return CloseOrderResult(
            channel = channel,
            outTradeNo = command.outTradeNo,
            accepted = response.statusCode in 200..299,
            raw = response.body
        )
    }

    override fun refund(command: RefundCommand): RefundResult {
        val ctx = resolveMerchant(command.merchantId, command.subMerchantId)
        val payload = buildRefundPayload(command, ctx)
        val response = execute(ctx, HttpMethod.POST, "/v3/refund/domestic/refunds", payload)
        val body = parseBody(response, H5RefundsDecodeVo::class.java)
        val status = mapRefundStatus(body?.refund_status)
        val channel = command.channelHint ?: PaymentChannel.WECHAT_MINI_APP
        return RefundResult(
            channel = channel,
            status = status,
            outRefundNo = body?.out_refund_no ?: command.outRefundNo,
            providerRefundId = body?.refund_id,
            raw = body
        )
    }

    private fun createJsapiOrder(command: CreateOrderCommand, ctx: WxMerchantContext): CreateOrderResult {
        val path = if (ctx.isServiceProvider()) "/v3/pay/partner/transactions/jsapi" else "/v3/pay/transactions/jsapi"
        val payload = if (ctx.isServiceProvider()) {
            buildPartnerJsapiPayload(command, ctx)
        } else {
            buildDirectJsapiPayload(command, ctx)
        }
        val response = execute(ctx, HttpMethod.POST, path, payload)
        val result = parseBody(response, JsApiPayVo::class.java)
            ?: throw PaymentClientException("WeChat JSAPI create order returned empty body")
        val appId = if (ctx.isServiceProvider()) {
            ensureValue(ctx.subMerchant?.appId, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].app-id")
        } else {
            ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        }
        val credential = buildJsapiCredential(result.prepay_id, appId, ctx)
        return CreateOrderResult(
            channel = PaymentChannel.WECHAT_JSAPI,
            credential = credential,
            providerTransactionId = null,
            raw = result
        )
    }

    private fun createH5Order(command: CreateOrderCommand, ctx: WxMerchantContext): CreateOrderResult {
        val path = if (ctx.isServiceProvider()) "/v3/pay/partner/transactions/h5" else "/v3/pay/transactions/h5"
        val payload = if (ctx.isServiceProvider()) {
            buildPartnerH5Payload(command, ctx)
        } else {
            buildDirectH5Payload(command, ctx)
        }
        val response = execute(ctx, HttpMethod.POST, path, payload)
        val result = parseBody(response, H5PrepayVo::class.java)
            ?: throw PaymentClientException("WeChat H5 create order returned empty body")
        return CreateOrderResult(
            channel = PaymentChannel.WECHAT_H5,
            credential = mapOf("h5Url" to result.h5_url),
            providerTransactionId = null,
            raw = result
        )
    }

    private fun createNativeOrder(command: CreateOrderCommand, ctx: WxMerchantContext): CreateOrderResult {
        val path = if (ctx.isServiceProvider()) "/v3/pay/partner/transactions/native" else "/v3/pay/transactions/native"
        val payload = if (ctx.isServiceProvider()) {
            buildPartnerNativePayload(command, ctx)
        } else {
            buildDirectNativePayload(command, ctx)
        }
        val response = execute(ctx, HttpMethod.POST, path, payload)
        val result = mapper.readTree(response.body)
        val codeUrl = result.get("code_url")?.asText().orEmpty()
        if (codeUrl.isBlank()) throw PaymentClientException("WeChat native create order returned empty code_url")
        return CreateOrderResult(
            channel = PaymentChannel.WECHAT_NATIVE,
            credential = mapOf("codeUrl" to codeUrl),
            raw = response.body
        )
    }

    private fun createAppOrder(command: CreateOrderCommand, ctx: WxMerchantContext): CreateOrderResult {
        val path = if (ctx.isServiceProvider()) "/v3/pay/partner/transactions/app" else "/v3/pay/transactions/app"
        val payload = if (ctx.isServiceProvider()) {
            buildPartnerAppPayload(command, ctx)
        } else {
            buildDirectAppPayload(command, ctx)
        }
        val response = execute(ctx, HttpMethod.POST, path, payload)
        val result = mapper.readTree(response.body)
        val prepayId = result.get("prepay_id")?.asText().orEmpty()
        if (prepayId.isBlank()) throw PaymentClientException("WeChat app create order returned empty prepay_id")
        val appId = if (ctx.isServiceProvider()) {
            ensureValue(ctx.subMerchant?.appId, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].app-id")
        } else {
            ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        }
        val mchid = if (ctx.isServiceProvider()) {
            ensureValue(ctx.subMerchant?.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
        } else {
            ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        }
        val timestamp = create_timestamp()
        val nonce = create_pay_nonce()
        val pkg = "Sign=WXPay"
        val message = genPaySign(appId, timestamp, nonce, prepayId)
        val paySign = sign(ctx, message)
        val credential = mapOf(
            "appid" to appId,
            "partnerid" to mchid,
            "prepayid" to prepayId,
            "package" to pkg,
            "noncestr" to nonce,
            "timestamp" to timestamp,
            "sign" to paySign
        )
        return CreateOrderResult(
            channel = PaymentChannel.WECHAT_APP,
            credential = credential,
            raw = response.body
        )
    }

    private fun buildDirectJsapiPayload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val payerOpenId = command.payer?.openId ?: throw IllegalArgumentException("payer.openId is required for JSAPI order")
        val notifyUrl = determineNotifyUrl(command, ctx)
        val payload = mutableMapOf<String, Any?>()
        payload["appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = notifyUrl
        payload["amount"] = command.amount.toAmountMap()
        payload["payer"] = mapOf("openid" to payerOpenId)
        command.expireTime?.let { payload["time_expire"] = dateFormatter.format(it.atOffset(ZoneOffset.ofHours(8))) }
        buildAttach(command)?.let { payload["attach"] = it }
        buildSceneInfo(command.scene)?.let { payload["scene_info"] = it }
        buildGoodsDetail(command.goods)?.let { payload["detail"] = mapOf("goods_detail" to it) }
        if (command.supportInvoice) {
            payload["support_fapiao"] = true
        }
        return payload
    }

    private fun buildPartnerJsapiPayload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val sub = ctx.subMerchant ?: throw IllegalStateException("sub merchant missing for service provider")
        val payload = mutableMapOf<String, Any?>()
        payload["sp_appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["sp_mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["sub_appid"] = ensureValue(sub.appId, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].app-id")
        payload["sub_mchid"] = ensureValue(sub.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = determineNotifyUrl(command, ctx)
        payload["amount"] = command.amount.toAmountMap()
        val payer = mutableMapOf<String, Any?>()
        command.payer?.openId?.let { payer["sp_openid"] = it }
        val subOpenId = command.payer?.subOpenId ?: command.payer?.openId
            ?: throw IllegalArgumentException("payer.subOpenId is required for service provider JSAPI order")
        payer["sub_openid"] = subOpenId
        payload["payer"] = payer
        command.expireTime?.let { payload["time_expire"] = dateFormatter.format(it.atOffset(ZoneOffset.ofHours(8))) }
        buildAttach(command)?.let { payload["attach"] = it }
        buildSceneInfo(command.scene)?.let { payload["scene_info"] = it }
        buildGoodsDetail(command.goods)?.let { payload["detail"] = mapOf("goods_detail" to it) }
        if (command.supportInvoice) {
            payload["support_fapiao"] = true
        }
        return payload
    }

    private fun buildDirectH5Payload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val payload = mutableMapOf<String, Any?>()
        payload["appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = determineNotifyUrl(command, ctx)
        payload["amount"] = command.amount.toAmountMap()
        payload["scene_info"] = buildH5SceneInfo(command.scene)
        buildAttach(command)?.let { payload["attach"] = it }
        return payload
    }

    private fun buildPartnerH5Payload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val sub = ctx.subMerchant ?: throw IllegalStateException("sub merchant missing for service provider")
        val payload = mutableMapOf<String, Any?>()
        payload["sp_appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["sp_mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["sub_appid"] = ensureValue(sub.appId, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].app-id")
        payload["sub_mchid"] = ensureValue(sub.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = determineNotifyUrl(command, ctx)
        payload["amount"] = command.amount.toAmountMap()
        payload["scene_info"] = buildH5SceneInfo(command.scene)
        buildAttach(command)?.let { payload["attach"] = it }
        return payload
    }

    private fun buildDirectNativePayload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val payload = mutableMapOf<String, Any?>()
        payload["appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = determineNotifyUrl(command, ctx)
        payload["amount"] = command.amount.toAmountMap()
        command.expireTime?.let { payload["time_expire"] = dateFormatter.format(it.atOffset(ZoneOffset.ofHours(8))) }
        buildAttach(command)?.let { payload["attach"] = it }
        buildGoodsDetail(command.goods)?.let { payload["detail"] = mapOf("goods_detail" to it) }
        return payload
    }

    private fun buildPartnerNativePayload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val sub = ctx.subMerchant ?: throw IllegalStateException("sub merchant missing for service provider")
        val payload = mutableMapOf<String, Any?>()
        payload["sp_appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["sp_mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["sub_appid"] = ensureValue(sub.appId, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].app-id")
        payload["sub_mchid"] = ensureValue(sub.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = determineNotifyUrl(command, ctx)
        payload["amount"] = command.amount.toAmountMap()
        command.expireTime?.let { payload["time_expire"] = dateFormatter.format(it.atOffset(ZoneOffset.ofHours(8))) }
        buildAttach(command)?.let { payload["attach"] = it }
        buildGoodsDetail(command.goods)?.let { payload["detail"] = mapOf("goods_detail" to it) }
        return payload
    }

    private fun buildDirectAppPayload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val payload = mutableMapOf<String, Any?>()
        payload["appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = determineNotifyUrl(command, ctx)
        payload["amount"] = command.amount.toAmountMap()
        command.expireTime?.let { payload["time_expire"] = dateFormatter.format(it.atOffset(ZoneOffset.ofHours(8))) }
        buildAttach(command)?.let { payload["attach"] = it }
        buildGoodsDetail(command.goods)?.let { payload["detail"] = mapOf("goods_detail" to it) }
        return payload
    }

    private fun buildPartnerAppPayload(command: CreateOrderCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val sub = ctx.subMerchant ?: throw IllegalStateException("sub merchant missing for service provider")
        val payload = mutableMapOf<String, Any?>()
        payload["sp_appid"] = ensureValue(ctx.merchant.appId, "wx.payment.merchants[${ctx.merchantId}].app-id")
        payload["sp_mchid"] = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        payload["sub_appid"] = ensureValue(sub.appId, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].app-id")
        payload["sub_mchid"] = ensureValue(sub.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
        payload["description"] = command.description
        payload["out_trade_no"] = command.outTradeNo
        payload["notify_url"] = determineNotifyUrl(command, ctx)
        payload["amount"] = command.amount.toAmountMap()
        command.expireTime?.let { payload["time_expire"] = dateFormatter.format(it.atOffset(ZoneOffset.ofHours(8))) }
        buildAttach(command)?.let { payload["attach"] = it }
        buildGoodsDetail(command.goods)?.let { payload["detail"] = mapOf("goods_detail" to it) }
        return payload
    }

    private fun buildClosePayload(ctx: WxMerchantContext): Map<String, Any?> {
        return if (ctx.isServiceProvider()) {
            mapOf(
                "sp_mchid" to ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid"),
                "sub_mchid" to ensureValue(ctx.subMerchant?.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
            )
        } else {
            mapOf("mchid" to ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid"))
        }
    }

    private fun buildRefundPayload(command: RefundCommand, ctx: WxMerchantContext): Map<String, Any?> {
        val payload = mutableMapOf<String, Any?>()
        command.transactionId?.let { payload["transaction_id"] = it }
        command.outTradeNo?.let { payload["out_trade_no"] = it }
        payload["out_refund_no"] = command.outRefundNo
        payload["amount"] = mapOf(
            "refund" to command.refundAmount.amountValue(),
            "total" to command.totalAmount.amountValue(),
            "currency" to command.refundAmount.currency
        )
        payload["notify_url"] = command.notifyUrl
            ?: ctx.subMerchant?.refundsNotifyUrl
            ?: ctx.merchant.refundsNotifyUrl
            ?: throw IllegalArgumentException("refund notify_url missing")
        command.reason?.let { payload["reason"] = it }
        buildGoodsDetail(command.goods)?.let { payload["goods_detail"] = it }
        if (ctx.isServiceProvider()) {
            payload["sub_mchid"] = ensureValue(ctx.subMerchant?.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
        }
        return payload
    }

    private fun buildQueryOutTradeNoPath(ctx: WxMerchantContext, outTradeNo: String): String {
        return if (ctx.isServiceProvider()) {
            val spMchid = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
            val subMchid = ensureValue(ctx.subMerchant?.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
            "/v3/pay/partner/transactions/out-trade-no/$outTradeNo?sp_mchid=${spMchid}&sub_mchid=${subMchid}"
        } else {
            val mchid = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
            "/v3/pay/transactions/out-trade-no/$outTradeNo?mchid=$mchid"
        }
    }

    private fun buildQueryTransactionPath(ctx: WxMerchantContext, transactionId: String): String {
        return if (ctx.isServiceProvider()) {
            val spMchid = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
            val subMchid = ensureValue(ctx.subMerchant?.mchid, "wx.payment.merchants[${ctx.merchantId}].subMerchants[${ctx.subMerchantId}].mchid")
            "/v3/pay/partner/transactions/id/$transactionId?sp_mchid=${spMchid}&sub_mchid=${subMchid}"
        } else {
            val mchid = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
            "/v3/pay/transactions/id/$transactionId?mchid=$mchid"
        }
    }

    private fun buildClosePath(ctx: WxMerchantContext, outTradeNo: String): String {
        return if (ctx.isServiceProvider()) {
            "/v3/pay/partner/transactions/out-trade-no/$outTradeNo/close"
        } else {
            "/v3/pay/transactions/out-trade-no/$outTradeNo/close"
        }
    }

    private fun buildH5SceneInfo(scene: PaymentScene?): Map<String, Any?> {
        val clientIp = scene?.clientIp ?: throw IllegalArgumentException("scene.clientIp is required for H5 payment")
        val extra = scene.extra
        val wapUrl = extra["wap_url"] ?: throw IllegalArgumentException("scene.extra['wap_url'] is required for H5 payment")
        val wapName = extra["wap_name"] ?: throw IllegalArgumentException("scene.extra['wap_name'] is required for H5 payment")
        val h5Info = mutableMapOf<String, Any?>()
        h5Info["type"] = extra["type"] ?: "Wap"
        extra["app_name"]?.let { h5Info["app_name"] = it }
        extra["app_url"]?.let { h5Info["app_url"] = it }
        extra["bundle_id"]?.let { h5Info["bundle_id"] = it }
        extra["package_name"]?.let { h5Info["package_name"] = it }
        h5Info["wap_url"] = wapUrl
        h5Info["wap_name"] = wapName
        return mapOf(
            "payer_client_ip" to clientIp,
            "h5_info" to h5Info
        )
    }

    private fun buildSceneInfo(scene: PaymentScene?): Map<String, Any?>? {
        if (scene == null) return null
        val info = mutableMapOf<String, Any?>()
        scene.clientIp?.let { info["payer_client_ip"] = it }
        scene.deviceId?.let { info["device_id"] = it }
        val storeInfo = mutableMapOf<String, Any?>()
        scene.extra["store_id"]?.let { storeInfo["id"] = it }
        scene.extra["store_name"]?.let { storeInfo["name"] = it }
        scene.extra["store_area_code"]?.let { storeInfo["area_code"] = it }
        scene.extra["store_address"]?.let { storeInfo["address"] = it }
        if (storeInfo.isNotEmpty()) {
            info["store_info"] = storeInfo
        }
        return if (info.isEmpty()) null else info
    }

    private fun buildGoodsDetail(details: List<GoodsDetail>): List<Map<String, Any?>>? {
        if (details.isEmpty()) return null
        return details.map {
            val item = mutableMapOf<String, Any?>()
            item["merchant_goods_id"] = it.merchantGoodsId
            it.wechatpayGoodsId?.let { v -> item["wechatpay_goods_id"] = v }
            it.name?.let { v -> item["goods_name"] = v }
            item["quantity"] = it.quantity
            item["unit_price"] = it.unitPrice
            item
        }
    }

    private fun buildJsapiCredential(prepayId: String, appId: String, ctx: WxMerchantContext): Map<String, Any?> {
        val timestamp = create_timestamp()
        val nonce = create_pay_nonce()
        val pkg = "prepay_id=$prepayId"
        val signature = sign(ctx, genPaySign(appId, timestamp, nonce, pkg))
        return mapOf(
            "appId" to appId,
            "timeStamp" to timestamp,
            "nonceStr" to nonce,
            "package" to pkg,
            "signType" to "RSA",
            "paySign" to signature
        )
    }

    private fun buildAttach(command: CreateOrderCommand): String? {
        return if (command.attach.isNotEmpty()) {
            mapper.writeValueAsString(command.attach)
        } else null
    }

    private fun determineNotifyUrl(command: CreateOrderCommand, ctx: WxMerchantContext): String {
        return command.notifyUrl
            ?: ctx.subMerchant?.notifyUrl
            ?: ctx.merchant.notifyUrl
            ?: throw IllegalArgumentException("notify_url must be provided for merchant ${ctx.merchantId}")
    }

    private fun mapTradeState(tradeState: String?): PaymentStatus {
        return when (tradeState) {
            "SUCCESS" -> PaymentStatus.SUCCESS
            "NOTPAY", "USERPAYING" -> PaymentStatus.PROCESSING
            "CLOSED", "REVOKED" -> PaymentStatus.CLOSED
            "REFUND" -> PaymentStatus.REFUNDING
            else -> PaymentStatus.UNKNOWN
        }
    }

    private fun mapRefundStatus(status: String?): PaymentStatus {
        return when (status) {
            "SUCCESS" -> PaymentStatus.SUCCESS
            "PROCESSING" -> PaymentStatus.PROCESSING
            "ABNORMAL" -> PaymentStatus.UNKNOWN
            "CLOSED" -> PaymentStatus.CLOSED
            else -> PaymentStatus.UNKNOWN
        }
    }

    private fun execute(ctx: WxMerchantContext, method: HttpMethod, path: String, body: Any?): HttpResult {
        val payload = when (body) {
            null -> null
            is String -> body
            else -> mapper.writeValueAsString(body)
        }
        val signBody = payload ?: ""
        val token = buildAuthorization(ctx, method.name, path, signBody)
        val headers = getPayHeaders(token)
        val entity: HttpEntity<*> = if (payload == null) {
            HttpEntity<Void>(headers)
        } else {
            HttpEntity(payload, headers)
        }
        val url = baseUrl(ctx) + path
        val response: ResponseEntity<String> = restTemplate.exchange(url, method, entity, String::class.java)
        val bodyText = response.body ?: ""
        if (response.statusCode.isError) {
            throw PaymentClientException("WeChat API call failed (${response.statusCode.value()}): $bodyText")
        }
        return HttpResult(response.statusCode.value(), bodyText, response.headers)
    }

    private fun <T> parseBody(response: HttpResult, responseType: Class<T>): T? {
        if (response.body.isBlank()) return null
        return mapper.readValue(response.body, responseType)
    }

    private fun buildAuthorization(ctx: WxMerchantContext, method: String, path: String, body: String): String {
        val mchid = ensureValue(ctx.merchant.mchid, "wx.payment.merchants[${ctx.merchantId}].mchid")
        val serialNo = ensureValue(ctx.merchant.serialNo, "wx.payment.merchants[${ctx.merchantId}].serial-no")
        val nonce = create_pay_nonce()
        val timestamp = create_timestamp()
        val message = genPaySign(method, path, timestamp, nonce, body)
        val signature = sign(ctx, message)
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"$mchid\",nonce_str=\"$nonce\",timestamp=\"$timestamp\",serial_no=\"$serialNo\",signature=\"$signature\""
    }

    private fun sign(ctx: WxMerchantContext, message: String): String {
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(resolvePrivateKey(ctx))
        signer.update(message.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(signer.sign())
    }

    private fun resolvePrivateKey(ctx: WxMerchantContext): PrivateKey {
        return privateKeyCache.computeIfAbsent(ctx.merchantId) {
            val pem = when {
                !ctx.merchant.privateKey.isNullOrBlank() -> ctx.merchant.privateKey!!
                !ctx.merchant.keyPath.isNullOrBlank() -> FileInputStream(ctx.merchant.keyPath).use { readIns(it) }
                else -> throw IllegalArgumentException("Private key not configured for merchant ${ctx.merchantId}")
            }
            val normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\\s+".toRegex(), "")
            val spec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(normalized))
            KeyFactory.getInstance("RSA").generatePrivate(spec)
        }
    }

    private fun Money.amountValue(): Int {
        require(total in 0..Int.MAX_VALUE) { "Amount $total exceeds supported range" }
        return total.toInt()
    }

    private fun Money.toAmountMap(): Map<String, Any?> {
        return mapOf(
            "total" to this.amountValue(),
            "currency" to this.currency
        )
    }

    private fun ensureValue(value: String?, field: String): String {
        return value?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("$field must be configured")
    }

    private fun baseUrl(ctx: WxMerchantContext): String {
        val domain = ctx.merchant.domain.ifBlank { "https://api.mch.weixin.qq.com" }
        return domain.trimEnd('/')
    }

    fun <T> parseNotification(
        merchantId: String?,
        subMerchantId: String?,
        headers: HttpHeaders,
        body: String,
        responseType: Class<T>
    ): T? {
        val ctx = resolveMerchant(merchantId, subMerchantId)
        if (!verifyNotification(ctx, headers, body)) {
            logger.warn("WeChat notification signature verify failed, merchant={} sub={}", ctx.merchantId, ctx.subMerchantId)
            return null
        }
        val resource = mapper.readTree(body).path("resource")
        val plainText = WxPayAes.decryptToString(
            resource.path("associated_data").asText(),
            resource.path("nonce").asText(),
            resource.path("ciphertext").asText(),
            ensureValue(ctx.merchant.apiV3Key, "wx.payment.merchants[${ctx.merchantId}].api-v3-key")
        )
        if (plainText.isNullOrBlank()) return null
        return mapper.readValue(plainText, responseType)
    }

    fun parsePaymentNotification(
        merchantId: String?,
        subMerchantId: String?,
        headers: HttpHeaders,
        body: String
    ): H5PayDecodeVo? = parseNotification(merchantId, subMerchantId, headers, body, H5PayDecodeVo::class.java)

    fun parseRefundNotification(
        merchantId: String?,
        subMerchantId: String?,
        headers: HttpHeaders,
        body: String
    ): H5RefundsDecodeVo? = parseNotification(merchantId, subMerchantId, headers, body, H5RefundsDecodeVo::class.java)

    private fun verifyNotification(ctx: WxMerchantContext, headers: HttpHeaders, body: String): Boolean {
        val timestamp = headers.getFirst("Wechatpay-Timestamp")
        val nonce = headers.getFirst("Wechatpay-Nonce")
        val signature = headers.getFirst("Wechatpay-Signature")
        val serial = headers.getFirst("Wechatpay-Serial")
        if (timestamp.isNullOrBlank() || nonce.isNullOrBlank() || signature.isNullOrBlank() || serial.isNullOrBlank()) {
            logger.warn("Missing notification signature headers: ts={}, nonce={}, sign={}, serial={}", timestamp, nonce, signature, serial)
            return false
        }
        val certificate = ensurePlatformCertificate(ctx, serial) ?: return false
        val message = genPaySign(timestamp, nonce, body)
        return try {
            val verifier = Signature.getInstance("SHA256withRSA")
            verifier.initVerify(certificate)
            verifier.update(message.toByteArray(StandardCharsets.UTF_8))
            verifier.verify(Base64.getDecoder().decode(signature))
        } catch (ex: Exception) {
            logger.error("Notification signature verify failed", ex)
            false
        }
    }

    private fun ensurePlatformCertificate(ctx: WxMerchantContext, serial: String): java.security.cert.X509Certificate? {
        val key = "${ctx.merchantId}:$serial"
        platformCertCache[key]?.let { return it }
        refreshCertificates(ctx)
        return platformCertCache[key]
    }

    private fun refreshCertificates(ctx: WxMerchantContext) {
        val cacheKey = ctx.merchantId
        val now = Instant.now()
        val last = lastCertRefresh[cacheKey]
        val intervalSeconds = ctx.merchant.certRefreshMinutes.coerceAtLeast(1).coerceAtMost(1440) * 60
        if (last != null && now.isBefore(last.plusSeconds(intervalSeconds))) {
            return
        }
        val response = runCatching { execute(ctx, HttpMethod.GET, "/v3/certificates", null) }.getOrElse {
            logger.warn("Refresh certificates failed for merchant {}", cacheKey, it)
            return
        }
        val certs = parseBody(response, PayCertResVo::class.java) ?: return
        certs.data.forEach { cert ->
            val decrypted = WxPayAes.decryptToString(
                cert.encrypt_certificate.associated_data,
                cert.encrypt_certificate.nonce,
                cert.encrypt_certificate.ciphertext,
                ensureValue(ctx.merchant.apiV3Key, "wx.payment.merchants[${ctx.merchantId}].api-v3-key")
            ) ?: return@forEach
            val x509 = java.security.cert.CertificateFactory.getInstance("X.509")
                .generateCertificate(decrypted.byteInputStream()) as java.security.cert.X509Certificate
            x509.checkValidity()
            val key = "${ctx.merchantId}:${cert.serial_no}"
            platformCertCache[key] = x509
        }
        lastCertRefresh[cacheKey] = now
    }

    private fun WxMerchantContext.isServiceProvider(): Boolean =
        merchant.mode == WxMerchantMode.SERVICE_PROVIDER

    private fun resolveMerchant(merchantId: String?, subMerchantId: String?): WxMerchantContext {
        val resolvedMerchantId = merchantId ?: properties.defaultMerchant
        require(!resolvedMerchantId.isNullOrBlank()) { "Unable to resolve merchantId, please configure wx.payment.default-merchant" }
        val merchantProps = properties.merchants[resolvedMerchantId]
            ?: throw IllegalArgumentException("Cannot find WeChat merchant config for id=$resolvedMerchantId")

        val isServiceProvider = merchantProps.mode == WxMerchantMode.SERVICE_PROVIDER
        if (!isServiceProvider && !subMerchantId.isNullOrBlank()) {
            throw IllegalArgumentException("Merchant $resolvedMerchantId is direct mode, subMerchantId should be null")
        }

        val subMerchantProps = if (isServiceProvider) {
            val id = subMerchantId ?: throw IllegalArgumentException("subMerchantId is required for service provider merchant $resolvedMerchantId")
            merchantProps.subMerchants[id]
                ?: throw IllegalArgumentException("Cannot find WeChat sub-merchant config for id=$id")
        } else null

        return WxMerchantContext(resolvedMerchantId, merchantProps, subMerchantId, subMerchantProps)
    }
}

data class WxMerchantContext(
    val merchantId: String,
    val merchant: WxMerchantProperties,
    val subMerchantId: String?,
    val subMerchant: WxSubMerchantProperties?
)

private class HttpResult(val statusCode: Int, val body: String, val headers: HttpHeaders)
