package com.ab.wx.wx_lib.vo.wx

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

/**
 * {
 *     "subscribe": 1,
 *     "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M",
 *     "language": "zh_CN",
 *     "subscribe_time": 1382694957,
 *     "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL",
 *     "remark": "",
 *     "groupid": 0,
 *     "tagid_list":[128,2],
 *     "subscribe_scene": "ADD_SCENE_QR_CODE",
 *     "qr_scene": 98765,
 *     "qr_scene_str": ""
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WxGetUserInfoVo(
    val subscribe: Int = 1,
    val openid: String = "",
    val language: String? = null,
    val subscribe_time: Long? = null,
    val unionid: String? = null,
    val remark: String? = null,
    val groupid: Int? = null,
    val tagid_list: List<Int>? = null,
    val subscribe_scene: String? = null,
    val qr_scene: String? = null,
    val qr_scene_str: String? = null
) : Serializable
