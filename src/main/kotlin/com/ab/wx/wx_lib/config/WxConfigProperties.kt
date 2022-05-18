package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(value ="wx")
object WxConfigProperties {

     lateinit var appId: String
     lateinit var appSec: String
     lateinit var miniAppId: String
     lateinit var miniAppSec: String

}