package com.ab.wx.wx_lib.vo.pay

import com.ab.wx.wx_lib.enums.TransMoneyStatusEnums
import java.io.Serializable

data class TransferVo(
    val out_batch_no: String?,
    val batch_id: String?,
    val create_time: String?,
    val batch_status: TransMoneyStatusEnums = TransMoneyStatusEnums.WAIT_PAY
) : Serializable
