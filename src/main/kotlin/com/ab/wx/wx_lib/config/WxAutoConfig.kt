package com.ab.wx.wx_lib.config

import jakarta.annotation.Resource
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(WxConfigProperties::class)
class WxAutoConfig {

    @Resource
    private lateinit var wxConfigProperties: WxConfigProperties


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