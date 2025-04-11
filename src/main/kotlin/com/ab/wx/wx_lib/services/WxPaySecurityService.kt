package com.ab.wx.wx_lib.services

interface WxPaySecurityService {
    /**
     * 获取签名
     * @param method HTTP方法
     * @param url 请求URL
     * @param body 请求体
     * @return 签名结果
     */
    fun generateSignature(method: String, url: String, body: String): String

    /**
     * 验证回调签名
     * @param body 回调消息体
     * @param signature 签名字符串
     * @param serialNo 证书序列号
     * @return 验证结果
     */
    fun verifySignature(body: String, signature: String, serialNo: String): Boolean

    /**
     * 加密敏感信息
     * @param content 需要加密的内容
     * @return 加密后的内容
     */
    fun encryptSensitiveInfo(content: String): String

    /**
     * 解密回调数据
     * @param associatedData 附加数据
     * @param nonce 随机串
     * @param ciphertext 密文
     * @return 解密后的明文
     */
    fun decryptData(associatedData: String, nonce: String, ciphertext: String): String
}