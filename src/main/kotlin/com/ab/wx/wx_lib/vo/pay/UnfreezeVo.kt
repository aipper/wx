package com.ab.wx.wx_lib.vo.pay

import java.time.OffsetDateTime

data class UnfreezeVo(
    val transaction_id: String = "",
    val out_order_no: String = "",
    val order_id: String = "",
    val state: String = "",
    val receivers: List<UnfreezeReceiver> = emptyList()
)

data class UnfreezeReceiver(
    val amount: Int = 0,
    val description: String = "",
    val type: String = "",
    val account: String = "",
    val result: UnfreeezeResultEnums? = null,
    val fail_reason: String = "",
    val create_time: OffsetDateTime? = null,
    val finish_time: OffsetDateTime? = null,
    val detail_id: String = ""
)

enum class UnfreeezeResultEnums {
    PENDING, SUCCESS, CLOSED
}
