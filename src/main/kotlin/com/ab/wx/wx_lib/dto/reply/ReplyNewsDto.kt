package com.ab.wx.wx_lib.dto.reply

import com.ab.wx.wx_lib.enums.MsgTypeEnums
import com.ab.wx.wx_lib.fn.getNowStr
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "xml")
data class ReplyNewsDto(
    @JacksonXmlProperty(localName = "FromUserName") val fromUserName: String = "",
    @JacksonXmlProperty(localName = "ToUserName") val toUserName: String = "",
    @JacksonXmlProperty(localName = "CreateTime") val createTime: String = "${getNowStr()}",
    @JacksonXmlProperty(localName = "Articles") val articles: List<NewsItem> = arrayListOf()

) {
    @get:JacksonXmlProperty(localName = "ArticleCount")
    val articleCount: Int by lazy { articles.size }

    @JacksonXmlProperty(localName = "MsgType")
    val msgType: String = MsgTypeEnums.NEWS.code
}


data class MINIPROGRAMPAGE(
    val title: String = "", val appid: String = "", val pagepath: String = "", val thumb_media_id: String = ""
)

data class NewsItem(
    val title: String = "", val description: String = "", val picUrl: String = "", val url: String = ""
)
