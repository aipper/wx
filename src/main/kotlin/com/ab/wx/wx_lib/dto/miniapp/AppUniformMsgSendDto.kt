package com.ab.wx.wx_lib.dto.miniapp

/**
 * 小程序统一消息体
 */
data class AppUniformMsgSendDto(
    var access_token: String = "",
    var touser: String = "",
    var mp_template_msg: Any? = null
)

data class MpTemplateMsg(
    var appid: String = "",
    var template_id: String = "",
    var url: String = "",
    var miniprogram: String = "",
    var data: String = ""
)

data class AppUniformMsgVo(
    val errcode: Int = 0,
    val errmsg: String = ""
)