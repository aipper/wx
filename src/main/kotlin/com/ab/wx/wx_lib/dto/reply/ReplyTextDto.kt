package com.ab.wx.wx_lib.dto.reply

import com.ab.wx.wx_lib.enums.MsgTypeEnums

data class ReplyTextDto(
    val fromUserName: String = "", val toUserName: String = "", val content: String = ""
) {

    val msgType: String = MsgTypeEnums.TEXT.code
}
