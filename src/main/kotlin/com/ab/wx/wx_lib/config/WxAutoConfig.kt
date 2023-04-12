package com.ab.wx.wx_lib.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.annotation.Resource

@Configuration
@EnableConfigurationProperties(WxConfigProperties::class, WxPayConfigProperties::class)
class WxAutoConfig {
    private val logger = LoggerFactory.getLogger(WxAutoConfig::class.java)

    @Resource
    private lateinit var wxConfigProperties: WxConfigProperties

    @Resource
    private lateinit var wxPayConfigProperties: WxPayConfigProperties
//
//    @Bean
//    @ConditionalOnMissingBean(Wx::class)
//    fun wx(): Wx {
//        logger.info("properties:$wxConfigProperties")
//        val wx = Wx(wxConfigProperties)
//        wx.genToken()
//        return wx
//    }
//
//    @Bean
//    @ConditionalOnMissingBean(MiniApp::class)
//    fun miniApp(): MiniApp {
//        val miniApp = MiniApp(wxConfigProperties)
//        miniApp.getAccessToken()
//        return miniApp
//    }
}