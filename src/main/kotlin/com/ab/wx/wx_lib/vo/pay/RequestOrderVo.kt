package com.ab.wx.wx_lib.vo.pay

data class RequestOrderVo(
    val transaction_id: String = "",
    val out_order_no: String = "",
    val order_id: String = "",
    val state: String = "",
    val receivers: List<RequestOrderReceive> = arrayListOf()
)

data class RequestOrderReceive(
    val amount: String = "",
    val description: String = "",
    val type: String = "",
    val account: String = "",
    val result: String = "",
    val fail_reason: String = "",
    val create_time: String = "",
    val finish_time: String = "",
    val detail_id: String = ""
)
