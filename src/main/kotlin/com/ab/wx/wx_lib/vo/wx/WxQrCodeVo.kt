package com.ab.wx.wx_lib.vo.wx

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * {"ticket":"gQH47joAAAAAAAAAASxodHRwOi8vd2VpeGluLnFxLmNvbS9xL2taZ2Z3TVRtNzJXV1Brb3ZhYmJJAAIEZ23sUwMEmm
 * 3sUw==","expire_seconds":60,"url":"http://weixin.qq.com/q/kZgfwMTm72WWPkovabbI"}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WxQrCodeVo(
    val ticket: String = "", val expire_seconds: Long = 0, val url: String = ""
)
