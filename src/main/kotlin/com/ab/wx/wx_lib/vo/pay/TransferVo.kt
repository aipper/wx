package com.ab.wx.wx_lib.vo.pay

data class TransferVo(
    val out_batch_no: String, val batch_id: String, val create_time: String, val batch_status: String = ""
)
