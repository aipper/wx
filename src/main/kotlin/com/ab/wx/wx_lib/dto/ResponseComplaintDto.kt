package com.ab.wx.wx_lib.dto

/**
 * 投诉处理-回复/完成 请求体
 * 说明：
 * - 回复投诉：支持附加图片、跳转链接、小程序跳转信息；字段为可选，未提供则不序列化（ObjectMapper 已配置 NON_NULL）。
 * - 完成投诉：通常仅需 complainted_mchid；多余字段将被微信侧忽略。
 */
data class ResponseComplaintDto(
    /** 被投诉商户号（服务商模式为子商户号） */
    val complainted_mchid: String = "",

    /** 回复内容，按官方字数限制（不同文档页约 200-500 字） */
    val response_content: String? = null,

    /** 证据图片 media_id 列表（通过媒体上传接口获取） */
    val response_images: List<String>? = null,

    /** H5 跳转链接（HTTPS） */
    val jump_url: String? = null,

    /** 跳转文案 */
    val jump_url_text: String? = null,

    /** 小程序跳转信息 */
    val mini_program_jump_info: MiniProgramJumpInfo? = null
)

/** 小程序跳转信息 */
data class MiniProgramJumpInfo(
    val appid: String? = null,
    val path: String? = null,
    val text: String? = null
)
