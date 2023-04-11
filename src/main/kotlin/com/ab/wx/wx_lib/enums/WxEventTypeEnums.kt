package com.ab.wx.wx_lib.enums

enum class WxEventTypeEnums(val code: String, val desc: String) {
    SUBSCRIBE("subscribe", "关注"), UNSUBSCRIBE("unsubscribe", "取消"), SCAN(
        "SCAN",
        "扫描带参数二维码事件"
    ),
    LOCATION("LOCATION", "上报地理位置事件"), CLICK("CLICK", "点击菜单事件"), VIEW(
        "VIEW",
        "点击菜单跳转链接时的事件推送"
    )
}