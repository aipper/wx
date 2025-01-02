package com.ab.wx.wx_lib.vo.miniapp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class Code2SessionVo(
    val openid: String = "",
    val session_key: String = "",
    val unionid: String = "",
    val errcode: String = "",
    val errmsg: String = ""
) : Serializable
