package com.ab.wx.wx_lib.dto.pay

import com.ab.wx.wx_lib.enums.AddReceiverEnums
import com.ab.wx.wx_lib.enums.RelationTypeEnums

data class DelReceiverDto(
    val appid: String = "",
    val type: AddReceiverEnums = AddReceiverEnums.MERCHANT_ID,
    val account: String = ""
)
