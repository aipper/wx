package com.ab.wx.wx_lib.exception
/**
 * 微信支付异常类定义
 */
sealed class WxPayException(message: String, cause: Throwable? = null) : Exception(message, cause)
class WxPayApiException(val errorCode: String, message: String, cause: Throwable? = null) : WxPayException(message, cause)
class WxPaySignatureException(message: String, cause: Throwable? = null) : WxPayException(message, cause)
class WxPayCertificateException(message: String, cause: Throwable? = null) : WxPayException(message, cause)
class WxPayConfigException(message: String, cause: Throwable? = null) : WxPayException(message, cause)