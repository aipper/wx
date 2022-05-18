package com.ab.wx.wx_lib.config

import com.ab.wx.wx_lib.wx.MiniApp
import com.ab.wx.wx_lib.wx.Wx
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.Resource

@Configuration
@EnableConfigurationProperties(WxConfigProperties::class)
class WxAutoConfig {
    @Resource
    private lateinit var wxConfigProperties: WxConfigProperties

    @Bean
    @ConditionalOnMissingBean(Wx::class)
    fun wx(): Wx {
        return Wx(wxConfigProperties)
    }

    @Bean
    @ConditionalOnMissingBean(MiniApp::class)
    fun miniApp(): MiniApp {
        return MiniApp(wxConfigProperties)
    }
}