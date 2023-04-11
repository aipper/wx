package com.ab.wx.wx_lib.dto.reply

import com.ab.wx.wx_lib.enums.MsgTypeEnums

data class ReplyVoiceDto(
    val fromUserName: String = "", val toUserName: String = "",
    val mediaId: String = ""
) {
    val msgType: String = MsgTypeEnums.VOICE.code
}