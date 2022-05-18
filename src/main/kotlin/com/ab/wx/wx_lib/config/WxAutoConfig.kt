package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(WxConfigProperties::class)
class WxAutoConfig {
}