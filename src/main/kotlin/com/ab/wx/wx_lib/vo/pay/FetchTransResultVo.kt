package com.ab.wx.wx_lib.vo.pay

import java.time.LocalDateTime

data class FetchTransResultVo(
    val transaction_id:String = "",
    val out_order_no:String = "",
    val order_id:String = "",
    val state: TransStateEnum = TransStateEnum.PROCESSING,
    val receivers: List<FetchTransResultReceivers> = emptyList()
)
enum class TransStateEnum {
    PROCESSING,FINISHED
}
data class FetchTransResultReceivers(
   val  amount: Int = 0,
    val description:String ,
    val type:String = "",
    val account:String = "",
    val result: TransResultEnums = TransResultEnums.PENDING,
    val fail_reason:String = "",
    val create_time: LocalDateTime,
    val finish_time: LocalDateTime,
    val detail_id:String = ""
)

enum class TransResultEnums{
    PENDING,SUCCESS,CLOSED
}