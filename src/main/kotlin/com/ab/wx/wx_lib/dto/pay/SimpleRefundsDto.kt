package com.ab.wx.wx_lib.dto.pay

data class SimpleRefundsDto(
    val orderId: String,
    val refundsOrderId: String,
    val refundsMoney: Int = 0,
    val totalMoney: Int = 0,
    )
