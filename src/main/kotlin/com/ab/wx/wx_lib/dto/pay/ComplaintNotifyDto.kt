package com.ab.wx.wx_lib.dto.pay

/**
 * 投诉通知回调配置请求体
 * 说明：
 * - 文档存在 url / callback_url 两种字段称谓，均提供可选字段，非空者将被序列化。
 * - 在服务商模式下，可能需要同时传递 mchid/sub_mchid（若走特定文档版本）。
 */
data class ComplaintNotifyDto(
    /** 通知回调地址（HTTPS） */
    val url: String? = null,

    /** 通知回调地址（部分文档页使用该字段名） */
    val callback_url: String? = null,

    /** 商户号（可选，按具体文档页要求使用） */
    val mchid: String? = null,

    /** 子商户号（服务商模式可选） */
    val sub_mchid: String? = null
)
