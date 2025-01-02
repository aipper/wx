package com.ab.wx.wx_lib.vo.miniapp

import java.io.Serializable

data class PhoneNumberVo(
    val errcode: Int? = null, val errmsg: String? = null, val phone_info: PhoneInfo? = null
) : Serializable

data class PhoneInfo(
    val phoneNumber: String = "",
    val purePhoneNumber: String = "",
    val countryCode: String = "",
    val watermark: Watermark? = null
) : Serializable

data class Watermark(
    val appid: String = "", val timestamp: Int = 0
) : Serializable