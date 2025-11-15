package com.ab.wx.wx_lib.payment.config

import com.ab.wx.wx_lib.payment.alipay.AlipayClient
import com.ab.wx.wx_lib.payment.api.PaymentClient
import com.ab.wx.wx_lib.payment.wechat.WechatPayClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [WxPaymentProperties::class, AlipayProperties::class])
class PaymentAutoConfiguration {

    @Bean("wechatPaymentClient")
    @ConditionalOnMissingBean(name = ["wechatPaymentClient"])
    @ConditionalOnClass(WechatPayClient::class)
    fun wechatPayClient(wxPaymentProperties: WxPaymentProperties): PaymentClient {
        return WechatPayClient(wxPaymentProperties)
    }

    @Bean("alipayPaymentClient")
    @ConditionalOnMissingBean(name = ["alipayPaymentClient"])
    @ConditionalOnClass(AlipayClient::class)
    fun alipayClient(alipayProperties: AlipayProperties): PaymentClient {
        return AlipayClient(alipayProperties)
    }
}
