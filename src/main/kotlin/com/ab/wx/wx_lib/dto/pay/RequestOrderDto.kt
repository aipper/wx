package com.ab.wx.wx_lib.dto.pay

import com.ab.wx.wx_lib.enums.AddReceiverEnums

data class RequestOrderDto(
    val appid: String = "",
    val transaction_id: String = "",
    val out_order_no: String = "",
    val receivers:List<Receiver> = arrayListOf(),
    val unfreeze_unsplit:Boolean = true,


    )

data class Receiver(
    val type: AddReceiverEnums = AddReceiverEnums.MERCHANT_ID,
    val account: String = "",
    val name: String = "",
    val amount: Int = 0,
    val description: String = ""
)