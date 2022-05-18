package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(value = WxConfigProperties.PREFIX)
object WxConfigProperties {
    const val PREFIX = "wx"

     lateinit var appId: String
     lateinit var appSec: String
     lateinit var miniAppId: String
     lateinit var miniAppSec: String

}