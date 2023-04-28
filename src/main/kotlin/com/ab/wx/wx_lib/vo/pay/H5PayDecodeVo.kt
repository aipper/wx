package com.ab.wx.wx_lib.vo.pay

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * {"mchid":"1638537256","appid":"wxdbbf793c5db4adf6",
 * "out_trade_no":"1651097350996779009","transaction_id":"4200001849202304265608716347",
 * "trade_type":"JSAPI","trade_state":"SUCCESS","trade_state_desc":"支付成功",
 * "bank_type":"CEB_CREDIT","attach":"","success_time":"2023-04-26T13:34:24+08:00",
 * "payer":{"openid":"ouxTn5g5nfRWcK4RV8y050dVhWXY"},"
 * amount":{"total":1,"payer_total":1,"currency":"CNY","payer_currency":"CNY"}}
 */
data class H5PayDecodeVo(
    val mchid: String = "",
    val appid: String = "",
    val out_trade_no: String = "",
    val transaction_id: String = "",
    val trade_type: String = "",
    val trade_state: String = "",
    val trade_state_desc: String = "",
    val bank_type: String = "",
    val attach: String = "",
    val success_time: String = "",
    val payer: Payer = Payer(),
    val amount: Amount = Amount()

)

data class Amount(
    val total: Int = 0,
    val payer_total: Int = 0,
    val currency: String = "",
    val payer_currency: String = ""
)

data class Payer(
    val openid: String = ""
)