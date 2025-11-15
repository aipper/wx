package com.ab.wx.wx_lib.payment.api

import java.time.Instant

enum class PaymentProvider {
    WECHAT,
    ALIPAY
}

enum class PaymentChannel {
    WECHAT_JSAPI,
    WECHAT_H5,
    WECHAT_NATIVE,
    WECHAT_APP,
    WECHAT_MINI_APP,
    WECHAT_QR_CODE,
    ALIPAY_APP,
    ALIPAY_PAGE,
    ALIPAY_WAP,
    ALIPAY_MINI_PROGRAM
}

enum class PaymentStatus {
    SUCCESS,
    PROCESSING,
    NOTPAY,
    CLOSED,
    REFUNDING,
    UNKNOWN
}

data class Money(val total: Long, val currency: String = "CNY")

data class Payer(
    val openId: String? = null,
    val subOpenId: String? = null,
    val userId: String? = null,
    val extra: Map<String, String> = emptyMap()
)

data class PaymentScene(
    val clientIp: String? = null,
    val deviceId: String? = null,
    val storeId: String? = null,
    val extra: Map<String, String> = emptyMap()
)

data class GoodsDetail(
    val merchantGoodsId: String,
    val wechatpayGoodsId: String? = null,
    val name: String? = null,
    val quantity: Int = 1,
    val unitPrice: Long = 0
)

data class CreateOrderCommand(
    val merchantId: String,
    val subMerchantId: String? = null,
    val channel: PaymentChannel,
    val outTradeNo: String,
    val description: String,
    val amount: Money,
    val payer: Payer? = null,
    val notifyUrl: String? = null,
    val attach: Map<String, String> = emptyMap(),
    val expireTime: Instant? = null,
    val scene: PaymentScene? = null,
    val goods: List<GoodsDetail> = emptyList(),
    val supportInvoice: Boolean = false
)

data class QueryOrderCommand(
    val merchantId: String,
    val subMerchantId: String? = null,
    val outTradeNo: String? = null,
    val transactionId: String? = null,
    val channelHint: PaymentChannel? = null
)

data class CloseOrderCommand(
    val merchantId: String,
    val subMerchantId: String? = null,
    val outTradeNo: String,
    val channelHint: PaymentChannel? = null
)

data class RefundCommand(
    val merchantId: String,
    val subMerchantId: String? = null,
    val outTradeNo: String? = null,
    val transactionId: String? = null,
    val outRefundNo: String,
    val refundAmount: Money,
    val totalAmount: Money,
    val reason: String? = null,
    val notifyUrl: String? = null,
    val goods: List<GoodsDetail> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val channelHint: PaymentChannel? = null
)

data class CreateOrderResult(
    val channel: PaymentChannel,
    val credential: Map<String, Any?>,
    val providerTransactionId: String? = null,
    val raw: Any? = null
)

data class QueryOrderResult(
    val channel: PaymentChannel,
    val status: PaymentStatus,
    val outTradeNo: String,
    val providerTransactionId: String? = null,
    val successTime: Instant? = null,
    val raw: Any? = null
)

data class CloseOrderResult(
    val channel: PaymentChannel,
    val outTradeNo: String,
    val accepted: Boolean,
    val raw: Any? = null
)

data class RefundResult(
    val channel: PaymentChannel,
    val status: PaymentStatus,
    val outRefundNo: String,
    val providerRefundId: String? = null,
    val raw: Any? = null
)
