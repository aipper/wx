package com.ab.wx.wx_lib.vo.pay

data class JsApiPayRes(
    val prepayId: String = "",
    val timestamp: String = "",
    val nonceStr: String = "",
    val signType: String = "RSA",
    val paySign: String? = "",
    val orderId:String=""
) {
}
