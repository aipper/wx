package com.ab.wx.wx_lib.enums

/**
 * 微信公众号消息类型
 */
enum class MsgTypeEnums(val code: String, val desc: String) {
    LOCATION("location", "地理位置"), LINK("link", "链接消息"), VIDEO("video", "视频消息"), SHORTVIDEO(
        "shortvideo",
        "小视频消息"
    ),
    VOICE("voice", "语音消息"), IMAGE("image", "图片消息"), TEXT(
        "text", "文本消息"
    ),
    EVENT("event", "事件推送"),MUSIC("music","音乐消息"), NEWS("news","图文消息"),
    MINIPROGRAMPAGE("miniprogrampage","小程序")
}