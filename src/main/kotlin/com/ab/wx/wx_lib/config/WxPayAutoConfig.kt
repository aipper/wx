package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.annotation.Resource

@Configuration
@EnableConfigurationProperties(WxPayConfigProperties::class)
class WxPayAutoConfig {

    @Resource
    private lateinit var wxPayConfigProperties: WxPayConfigProperties
}