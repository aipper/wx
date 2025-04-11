package com.ab.wx.wx_lib.vo.pay

data class RefundResult(
    val refundId: String,
    val outRefundNo: String,
    val transactionId: String,
    val outTradeNo: String,
    val status: String,
    val successTime: String? = null
)