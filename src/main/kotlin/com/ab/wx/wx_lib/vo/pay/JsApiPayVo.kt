package com.ab.wx.wx_lib.vo.pay

import java.io.Serializable

data class JsApiPayVo(
    /**
     * 预支付交易会话标识
     */
    val prepay_id: String = ""
):Serializable
