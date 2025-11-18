package com.ab.wx.wx_lib.config

import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

/**
 * 微信支付公钥配置类
 */
class WxPayPublicKeyConfig {
    private var wechatPayPublicKey: String? = null
    private var wechatPayPublicKeyId: String? = null
    private var publicKey: PublicKey? = null

    constructor(wechatPayPublicKey: String?, wechatPayPublicKeyId: String?) {
        this.wechatPayPublicKey = wechatPayPublicKey
        this.wechatPayPublicKeyId = wechatPayPublicKeyId
        this.publicKey = loadPublicKeyFromString(wechatPayPublicKey)
    }

    /**
     * 从字符串加载公钥
     */
    private fun loadPublicKeyFromString(keyString: String?): PublicKey? {
        return try {
            keyString?.let {
                val publicKeyPEM = it.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("\\s".toRegex(), "")
                
                val decodedKey = Base64.getDecoder().decode(publicKeyPEM)
                val keySpec = X509EncodedKeySpec(decodedKey)
                val keyFactory = KeyFactory.getInstance("RSA")
                keyFactory.generatePublic(keySpec)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("加载微信支付公钥失败", e)
        }
    }

    /**
     * 获取微信支付公钥
     */
    fun getPublicKey(): PublicKey? = publicKey

    /**
     * 获取微信支付公钥ID
     */
    fun getPublicKeyId(): String? = wechatPayPublicKeyId

    /**
     * 检查公钥配置是否有效
     */
    fun isValid(): Boolean {
        return publicKey != null && !wechatPayPublicKeyId.isNullOrBlank()
    }
}