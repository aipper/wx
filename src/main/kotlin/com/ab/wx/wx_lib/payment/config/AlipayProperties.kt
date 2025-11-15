package com.ab.wx.wx_lib.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "alipay.payment")
class AlipayProperties {
    /** 默认商户 ID（自定义标识），未显式传入时使用 */
    var defaultMerchant: String? = null
    var merchants: MutableMap<String, AlipayMerchantProperties> = linkedMapOf()
}

class AlipayMerchantProperties {
    var appId: String? = null
    var privateKey: String? = null
    var privateKeyPath: String? = null
    var alipayPublicKey: String? = null
    var alipayPublicKeyPath: String? = null
    var endpoint: String = "https://openapi.alipay.com/gateway.do"
    var notifyUrl: String? = null
    var returnUrl: String? = null
    var charset: String = "utf-8"
    var signType: String = "RSA2"
}
