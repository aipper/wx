package com.ab.wx.wx_lib.config

import com.ab.wx.wx_lib.wx.MiniApp
import com.ab.wx.wx_lib.wx.Wx
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.Resource

@Configuration
@EnableConfigurationProperties(WxConfigProperties::class)
class WxAutoConfig {
    private val logger = LoggerFactory.getLogger(WxAutoConfig::class.java)

    @Resource
    private lateinit var wxConfigProperties: WxConfigProperties

    @Bean
    @ConditionalOnMissingBean(Wx::class)
    fun wx(): Wx {
        logger.info("properties:$wxConfigProperties")
        val wx = Wx(wxConfigProperties)
        wx.genToken()
        return wx
    }

    @Bean
    @ConditionalOnMissingBean(MiniApp::class)
    fun miniApp(): MiniApp {
        val miniApp = MiniApp(wxConfigProperties)
        miniApp.getAccessToken()
        return miniApp
    }
}