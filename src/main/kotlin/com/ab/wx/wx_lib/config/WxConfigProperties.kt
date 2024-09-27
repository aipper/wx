package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "wx")
class WxConfigProperties {

    lateinit var appId: String
    lateinit var appSec: String
    lateinit var miniAppId: String
    lateinit var miniAppSec: String
    lateinit var miniAppToken: String
    lateinit var wxToken: String
    var debug: Boolean = false

    var aesKey: String? = null


    lateinit var miniAppIdTwo: String
    lateinit var miniAppSecTwo: String

    @get:ConfigurationProperties(prefix = "wx.pay")
    var pay: WxPayConfigProperties? = null

}