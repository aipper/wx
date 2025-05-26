package com.ab.wx.wx_lib.dto.pay

/**
 * 分流退回
 */
data class TransReturnDto(
    val order_id:String?=null,
    val out_order_no:String?=null,
    val out_return_no:String,
    val return_mchid:String,
    val amount: Int,
    val description: String
)
