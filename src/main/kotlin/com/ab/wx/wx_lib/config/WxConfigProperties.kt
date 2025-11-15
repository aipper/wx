package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "wx")
class WxConfigProperties {

    lateinit var appId: String
    lateinit var appSec: String
    lateinit var wxToken: String
    var aesKey: String? = null
    /**
     * 公众号服务器回调域名，可选
     */
    var callbackBaseUrl: String? = null

    /**
     * 小程序配置，当不需要小程序相关能力时可以不配置
     */
    var miniApp: MiniAppProperties? = null

    class MiniAppProperties {
        var enabled: Boolean = true
        var appId: String? = null
        var appSecret: String? = null
    }
}
