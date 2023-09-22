package com.ab.wx.wx_lib.dto.miniapp

data class MiniappMsgDto(
    val touser: String,
    val template_id: String,
    val page: String,
    val miniprogram_state: String = "formal",
    val lang: String = "zh_CN",
    val data: Any
)
