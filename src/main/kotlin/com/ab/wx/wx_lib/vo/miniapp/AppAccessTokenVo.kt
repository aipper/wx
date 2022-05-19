package com.ab.wx.wx_lib.vo.miniapp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class AppAccessTokenVo(
    val access_token:String="",
    val expires_in:Int = 0,
    val errcode:Int=0,
    val errmsg:String=""
)
