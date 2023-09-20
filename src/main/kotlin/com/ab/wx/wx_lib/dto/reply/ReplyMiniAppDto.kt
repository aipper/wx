package com.ab.wx.wx_lib.dto.reply

import com.ab.wx.wx_lib.enums.MsgTypeEnums
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
data class ReplyMiniAppDto(
    @JacksonXmlProperty(localName = "touser") val touser: String = "",
    @JacksonXmlProperty(localName = "miniprogrampage") val miniprogrampage: MINIPROGRAMPAGE = MINIPROGRAMPAGE(),
) {
    @JacksonXmlProperty(localName = "MsgType")
    val msgtype: String = MsgTypeEnums.MINIPROGRAMPAGE.code
}

data class MINIPROGRAMPAGE(
    val title: String = "", val appid: String = "", val pagepath: String = "", val thumb_media_id: String = ""
)