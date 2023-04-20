package com.ab.wx.wx_lib.dto.pay

data class SimplePayDto(
    val description: String = "",
    val orderNo: String = "",
    val notifyUrl: String = "",
    val amount: Int = 0,
    val payOpenid: String = ""
)
