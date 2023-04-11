package com.ab.wx.wx_lib.dto

data class WxSendTemplateDto(
    /**
     * 接收用户的openid
     */
    val touser: String = "",
    val template_id: String = "",
    val topcolor: String = "",
    val data: Any? = null
)
