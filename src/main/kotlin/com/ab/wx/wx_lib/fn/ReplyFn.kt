package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.dto.reply.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.io.InputStream
import java.time.Instant


fun getXmlMapper(): XmlMapper {
    return XmlMapper.builder().defaultUseWrapper(false)
        .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build()

}


/**
 * 转换普通消息
 */
fun readNormalXmlMsg(input: InputStream): ReceiveNormalTextMsg? {
    return getXmlMapper().readValue(input, ReceiveNormalTextMsg::class.java)
}

/**
 * 转换消息
 */
fun readCustomerXmlMsg(input: InputStream): ReceiveCustomerMsg? {
    return getXmlMapper().readValue(input, ReceiveCustomerMsg::class.java)
}


/**
 * 读取客户发过来的消息
 */

@JacksonXmlRootElement(localName = "xml")
data class ReceiveCustomerMsg(
    @JacksonXmlProperty(localName = "ToUserName") var toUserName: String? = null,
    @JacksonXmlProperty(localName = "FromUserName") var fromUserName: String? = null,
    @JacksonXmlProperty(localName = "CreateTime") var createTime: String? = null,
    @JacksonXmlProperty(localName = "MsgType") var msgType: String? = null,
    @JacksonXmlProperty(localName = "Content") var content: String? = null,
    @JacksonXmlProperty(localName = "PicUrl") var picUrl: String? = null,
    @JacksonXmlProperty(localName = "Format") var format: String? = null,
    @JacksonXmlProperty(localName = "Location_X") var location_X: String? = null,
    @JacksonXmlProperty(localName = "Location_Y") var location_Y: String? = null,
    @JacksonXmlProperty(localName = "Scale") var scale: String? = null,
    @JacksonXmlProperty(localName = "Label") var label: String? = null,
    @JacksonXmlProperty(localName = "Recognition") var recognition: String? = null,
    @JacksonXmlProperty(localName = "Title") var title: String? = null,
    @JacksonXmlProperty(localName = "Event") var event: String? = null,
    /**
     * 事件KEY值，qrscene_为前缀，后面为二维码的参数值
     *
     */
    @JacksonXmlProperty(localName = "EventKey") var eventKey: String? = null,
    /**
     * 二维码的ticket，可用来换取二维码图片
     *
     */
    @JacksonXmlProperty(localName = "Ticket") var ticket: String? = null,

    @JacksonXmlProperty(localName = "Description") var description: String? = null,
    @JacksonXmlProperty(localName = "Url") var url: String? = null,
    @JacksonXmlProperty(localName = "ThumbMediaId") var thumbMediaId: String? = null,
    @JacksonXmlProperty(localName = "MediaId") var mediaId: String? = null,
    @JacksonXmlProperty(localName = "MsgId") var msgId: String? = null,
    @JacksonXmlProperty(localName = "MsgDataId") var msgDataId: String? = null,
    @JacksonXmlProperty(localName = "Idx") var idx: String? = null,
)


/**
 * 接收普通消息
 * <xml>
 *   <ToUserName><![CDATA[toUser]]></ToUserName>
 *   <FromUserName><![CDATA[fromUser]]></FromUserName>
 *   <CreateTime>1348831860</CreateTime>
 *   <MsgType><![CDATA[text]]></MsgType>
 *   <Content><![CDATA[this is a test]]></Content>
 *   <MsgId>1234567890123456</MsgId>
 *   <MsgDataId>xxxx</MsgDataId>
 *   <Idx>xxxx</Idx>
 * </xml>
 *
 */
@JacksonXmlRootElement(localName = "xml")
data class ReceiveNormalTextMsg(
    @JacksonXmlProperty(localName = "ToUserName") var toUserName: String? = null,
    @JacksonXmlProperty(localName = "FromUserName") var fromUserName: String? = null,
    @JacksonXmlProperty(localName = "CreateTime") var createTime: String? = null,
    @JacksonXmlProperty(localName = "MsgType") var msgType: String? = null,
    @JacksonXmlProperty(localName = "Content") var content: String? = null,
    @JacksonXmlProperty(localName = "MsgId") var msgId: String? = null,
    @JacksonXmlProperty(localName = "MsgDataId") var msgDataId: String? = null,
    @JacksonXmlProperty(localName = "Idx") var idx: String? = null,
)

/**
 * 接收图文消息
 * <xml>
 *   <ToUserName><![CDATA[toUser]]></ToUserName>
 *   <FromUserName><![CDATA[fromUser]]></FromUserName>
 *   <CreateTime>1348831860</CreateTime>
 *   <MsgType><![CDATA[image]]></MsgType>
 *   <PicUrl><![CDATA[this is a url]]></PicUrl>
 *   <MediaId><![CDATA[media_id]]></MediaId>
 *   <MsgId>1234567890123456</MsgId>
 *    <MsgDataId>xxxx</MsgDataId>
 *   <Idx>xxxx</Idx>
 * </xml>
 */

@JacksonXmlRootElement(localName = "xml")
data class ReceivePicMsg(
    @JacksonXmlProperty(localName = "ToUserName") var toUserName: String? = null,
    @JacksonXmlProperty(localName = "FromUserName") var fromUserName: String? = null,
    @JacksonXmlProperty(localName = "CreateTime") var createTime: String? = null,
    @JacksonXmlProperty(localName = "MsgType") var msgType: String? = null,
    @JacksonXmlProperty(localName = "PicUrl") var picUrl: String? = null,
    @JacksonXmlProperty(localName = "MediaId") var mediaId: String? = null,
    @JacksonXmlProperty(localName = "MsgId") var msgId: String? = null,
    @JacksonXmlProperty(localName = "MsgDataId") var msgDataId: String? = null,
    @JacksonXmlProperty(localName = "Idx") var idx: String? = null,
)


/**
 *  回复文本消息
 */

fun replyTextMsg(dto: ReplyTextDto): String {
    return """
        <xml>
          <ToUserName>${dto.toUserName}</ToUserName>
          <FromUserName>${dto.fromUserName}</FromUserName>
          <CreateTime>${getNowStr()}</CreateTime>
          <MsgType>${dto.msgType}</MsgType>
          <Content>${dto.content}</Content>
        </xml>
    """.trimIndent()
}

/**
 * 回复图片消息
 */
fun replyPicMsg(dto: ReplyPicDto): String {
    return """
        <xml>
          <ToUserName>${dto.toUserName}</ToUserName>
          <FromUserName>${dto.fromUserName}</FromUserName>
          <CreateTime>${getNowStr()}</CreateTime>
          <MsgType>${dto.msgType}</MsgType>
          <Image>
            <MediaId>${dto.mediaId}</MediaId>
          </Image>
        </xml>
    """.trimIndent()
}

/**
 * 回复语音消息
 */
fun replyVoiceMsg(dto: ReplyVoiceDto): String {
    return """
         <xml>
          <ToUserName>${dto.toUserName}</ToUserName>
          <FromUserName>${dto.fromUserName}</FromUserName>
          <CreateTime>${getNowStr()}</CreateTime>
          <MsgType>${dto.msgType}</MsgType>
          <Voice>
            <MediaId>${dto.mediaId}</MediaId>
          </Voice>
        </xml>
    """.trimIndent()
}

/**
 * 回复视频消息
 */
fun replyVideoMsg(dto: ReplyVideoDto): String {
    return """
        <xml>
          <ToUserName>${dto.toUserName}</ToUserName>
          <FromUserName>${dto.fromUserName}</FromUserName>
          <CreateTime>${getNowStr()}</CreateTime>
          <MsgType>${dto.msgType}</MsgType>
          <Video>
            <MediaId>${dto.mediaId}</MediaId>
            <Title>${dto.title}</Title>
            <Description>${dto.description}</Description>
          </Video>
        </xml>
    """.trimIndent()
}

/**
 *  回复音乐消息
 */
fun replyMusicMsg(dto: ReplyMusicDto): String {
    return """
        <xml>
         <ToUserName>${dto.toUserName}</ToUserName>
          <FromUserName>${dto.fromUserName}</FromUserName>
          <CreateTime>${getNowStr()}</CreateTime>
          <MsgType>${dto.msgType}</MsgType>
          <Music>
            <Title>${dto.title}</Title>
            <Description>${dto.description}</Description>
            <MusicUrl>${dto.musicURL}</MusicUrl>
            <HQMusicUrl>${dto.hQMusicUrl}</HQMusicUrl>
            <ThumbMediaId>${dto.mediaId}</ThumbMediaId>
          </Music>
        </xml>
    """.trimIndent()
}

/**
 * 回复图文消息
 */
fun replyNewsMsg(dto: ReplyNewsDto): String? {
//    return getXmlMapper().writeValueAsString(dto)
    return """
        <xml>
          <ToUserName>${dto.toUserName}</ToUserName>
          <FromUserName>${dto.fromUserName}</FromUserName>
          <CreateTime>${getNowStr()}</CreateTime>
          <MsgType>${dto.msgType}</MsgType>
          <ArticleCount>${dto.articles.size}</ArticleCount>
          <Articles>
            ${genNewsItem(dto.articles)} 
          </Articles>
        </xml>

    """.trimIndent()
}

private fun genNewsItem(items: List<NewsItem>): String {
    val sb = StringBuffer()
    items.forEach {
        sb.append(
            """
             <item>
              <Title>${it.title}</Title>
              <Description>${it.description}</Description>
              <PicUrl>${it.picUrl}</PicUrl>
              <Url>${it.url}</Url>
            </item> 
        """.trimIndent()
        )
    }
    return sb.toString()
}


fun getNowStr() = Instant.now().epochSecond
