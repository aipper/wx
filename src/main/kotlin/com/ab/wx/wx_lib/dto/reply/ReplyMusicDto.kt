package com.ab.wx.wx_lib.dto.reply

import com.ab.wx.wx_lib.enums.MsgTypeEnums

data class ReplyMusicDto(
    val fromUserName: String = "",
    val toUserName: String = "",
    val mediaId: String = "",
    val title: String = "",
    val description: String = "",
    val thumbMediaId: String = "",
    val hQMusicUrl: String = "",
    val musicURL: String = ""
) {
    val msgType: String = MsgTypeEnums.VIDEO.code
}
