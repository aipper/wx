package com.ab.wx.wx_lib.vo.pay

import java.time.LocalDateTime

data class TransReturnVo(
    val order_id: String = "",
    val out_order_no: String = "",
    val out_return_no: String = "",
    val return_id: String = "",
    val return_mchid: String = "",
    val amount: Int = 0,
    val description: String = "",
    val result: TransReturnResultEnums,
    val fail_reason: String? = null,
    val create_time: LocalDateTime? = null,
    val finish_time: LocalDateTime? = null
)

enum class TransReturnResultEnums {
    PROCESSING, SUCCESS, FAILED
}