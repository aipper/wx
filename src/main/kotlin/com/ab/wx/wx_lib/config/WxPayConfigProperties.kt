package com.ab.wx.wx_lib.config


class WxPayConfigProperties {
    var mchid: String? = null
    var notifyUrl: String? = null
    var refundsNotifyUrl: String? = null
    var v3key: String? = null
    var keyPath: String = ""
    var serialNo: String = ""
    var transCallbackUrl: String? = null
    var publicKeyPath: String? = null
    var publicKeyNo: String? = null
    var profitSharingNotifyUrl: String? = null
    /**
     * 是否使用微信支付公钥模式（默认为false，使用证书模式）
     */
    var usePublicKeyMode: Boolean = false
    /**
     * 微信支付公钥内容（当usePublicKeyMode为true时使用）
     */
    var wechatPayPublicKey: String? = null
    /**
     * 微信支付公钥ID（当usePublicKeyMode为true时使用）
     */
    var wechatPayPublicKeyId: String? = null
}