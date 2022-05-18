package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(value = WxConfigProperties.prefix)
object WxConfigProperties {
    const val prefix = "wx"

    private lateinit var appId: String
    private lateinit var appSec: String
    private lateinit var miniAppId: String
    private lateinit var miniAppSec: String



}