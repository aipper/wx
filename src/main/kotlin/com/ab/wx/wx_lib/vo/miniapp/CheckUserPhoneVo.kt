package com.ab.wx.wx_lib.vo.miniapp

data class CheckUserPhoneVo(
    val errcode: Int = 0, val errmsg: String = "", val phone_info: PhoneInfoVo = PhoneInfoVo()
)


data class PhoneInfoVo(
    val phoneNumber: String = "",
    val purePhoneNumber: String = "",
    val countryCode: String = "",
    val watermark: Watermark = Watermark()
)

data class WaterMask(
    val timestamp: Long = 0, val appid: String = ""
)