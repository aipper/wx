package com.ab.wx.wx_lib.dto.pay

data class UnfreezeDto(
    val transaction_id:String = "",
    val out_order_no:String = "",
    val description:String = ""
)
