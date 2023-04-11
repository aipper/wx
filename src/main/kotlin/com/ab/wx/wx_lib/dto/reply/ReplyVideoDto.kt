package com.ab.wx.wx_lib.dto.reply

import com.ab.wx.wx_lib.enums.MsgTypeEnums

data class ReplyVideoDto(
    val fromUserName: String = "",
    val toUserName: String = "",
    val mediaId: String = "",
    val title: String = "",
    val description: String = ""

) {
    val msgType: String = MsgTypeEnums.VIDEO.code
}