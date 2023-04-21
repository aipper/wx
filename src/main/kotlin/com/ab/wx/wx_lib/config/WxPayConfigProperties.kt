package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties


class WxPayConfigProperties {
    var mchid: String? = null
    var notifyUrl: String? = null
    var v3key: String? = null
    var keyPath:String=""
    var serialNo:String=""
}