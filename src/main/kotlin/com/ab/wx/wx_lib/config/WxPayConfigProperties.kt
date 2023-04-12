package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "wx.pay")

class WxPayConfigProperties {
    lateinit var mchid: String
}