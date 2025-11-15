package com.ab.wx.wx_lib.config

import com.ab.wx.wx_lib.wx.MiniApp
import com.ab.wx.wx_lib.wx.Wx
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(Wx::class)
@EnableConfigurationProperties(WxConfigProperties::class)
class WxAutoConfig(private val wxConfigProperties: WxConfigProperties) {

    private val logger = LoggerFactory.getLogger(WxAutoConfig::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun wx(): Wx {
        logger.debug("Initializing Wx client with appId={}", wxConfigProperties.appId)
        return Wx(wxConfigProperties)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wx.mini-app", name = ["app-id", "app-secret"])
    fun miniApp(): MiniApp {
        val miniProps = wxConfigProperties.miniApp
            ?: throw IllegalStateException("wx.mini-app is configured but cannot be bound to WxConfigProperties")
        val appId = requireNotNull(miniProps.appId) { "wx.mini-app.app-id must be provided" }
        val secret = requireNotNull(miniProps.appSecret) { "wx.mini-app.app-secret must be provided" }
        logger.debug("Initializing MiniApp client with appId={}", appId)
        return MiniApp(appId, secret)
    }
}
