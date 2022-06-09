package com.ab.wx.wx_lib.vo.miniapp

data class PhoneNumberVo(
    val errcode: Int? = null,
    val errmsg: String? = null,
    val phone_info: PhoneInfo? = null
)

data class PhoneInfo(
    val phoneNumber: String = "",
    val purePhoneNumber: String = "",
    val countryCode: String = "",
    val watermark: Watermark? = null
)

data class Watermark(
    val appid: String = "",
    val timestamp: Int = 0
)