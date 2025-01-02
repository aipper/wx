package com.ab.wx.wx_lib.vo.wx

import java.io.Serializable

data class WxQrCodeUrlVo(
    val ticket:String="",
    val url:String="",
    val showUrl:String=""
):Serializable
