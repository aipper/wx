package com.ab.wx.wx_lib.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "wx.payment")
class WxPaymentProperties {
    /**
     * 默认商户标识，如果调用方未显式传入 merchantId，则会使用该值。
     */
    var defaultMerchant: String? = null

    /**
     * 多商户配置，key 通常是业务自定义的 merchantId。
     */
    var merchants: MutableMap<String, WxMerchantProperties> = linkedMapOf()
}

enum class WxMerchantMode {
    DIRECT,
    SERVICE_PROVIDER
}

class WxMerchantProperties {
    var description: String? = null
    var mode: WxMerchantMode = WxMerchantMode.DIRECT
    var appId: String? = null
    var mchid: String? = null
    var serialNo: String? = null
    var apiV3Key: String? = null
    var privateKey: String? = null
    var keyPath: String? = null
    var notifyUrl: String? = null
    var refundsNotifyUrl: String? = null
    var domain: String = "https://api.mch.weixin.qq.com"
    var subMerchants: MutableMap<String, WxSubMerchantProperties> = linkedMapOf()
    /**
     * 平台证书拉取的最小间隔（分钟），默认 10 分钟内不重复拉取。
     */
    var certRefreshMinutes: Long = 10
}

class WxSubMerchantProperties {
    var mchid: String? = null
    var appId: String? = null
    var notifyUrl: String? = null
    var refundsNotifyUrl: String? = null
}
