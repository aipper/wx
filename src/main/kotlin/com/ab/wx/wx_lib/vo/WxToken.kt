package com.ab.wx.wx_lib.vo

import java.io.Serializable


data class WxToken(var access_token: String = "", var expires_in: Int = 0) : Serializable