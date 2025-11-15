package com.ab.wx.wx_lib.config

import com.ab.wx.wx_lib.wx.WxPay
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(WxPay::class)
@EnableConfigurationProperties(value = [WxConfigProperties::class, WxPayConfigProperties::class])
class WxPayAutoConfig(
    private val wxConfigProperties: WxConfigProperties,
    private val wxPayConfigProperties: WxPayConfigProperties
) {

    private val logger = LoggerFactory.getLogger(WxPayAutoConfig::class.java)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wx.pay", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun wxPay(): WxPay {
        require(!wxPayConfigProperties.mchid.isNullOrBlank()) { "wx.pay.mchid must be configured" }
        require(wxPayConfigProperties.serialNo.isNotBlank()) { "wx.pay.serial-no must be configured" }
        require(!wxPayConfigProperties.apiV3Key.isNullOrBlank()) { "wx.pay.api-v3-key must be configured" }
        if (wxPayConfigProperties.privateKey.isNullOrBlank() && wxPayConfigProperties.keyPath.isNullOrBlank()) {
            throw IllegalStateException("Either wx.pay.private-key or wx.pay.key-path must be configured")
        }
        logger.debug("Initializing WxPay client for mchid={}", wxPayConfigProperties.mchid)
        return WxPay(wxConfigProperties, wxPayConfigProperties)
    }
}
