package com.ab.wx.wx_lib.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "wx.pay")
class WxPayConfigProperties {
    /**
     * 是否启用微信支付自动配置
     */
    var enabled: Boolean = true

    /**
     * 直连商户号
     */
    var mchid: String? = null

    /**
     * API 证书序列号
     */
    var serialNo: String = ""

    /**
     * APIv3 密钥
     */
    var apiV3Key: String? = null

    /**
     * PEM 格式私钥内容
     */
    var privateKey: String? = null

    /**
     * PEM 私钥文件路径，可在未提供 privateKey 时回退使用
     */
    var keyPath: String? = null

    var notifyUrl: String? = null
    var refundsNotifyUrl: String? = null

    /**
     * 是否自动刷新平台证书
     */
    var autoUpdateCertificate: Boolean = true

    /**
     * 证书刷新周期，默认 12 小时
     */
    var certificateTtl: Duration = Duration.ofHours(12)

    /**
     * API 域名，可根据需要覆盖
     */
    var apiHost: String = "https://api.mch.weixin.qq.com"
}
