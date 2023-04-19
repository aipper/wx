package com.ab.wx.wx_lib.vo.wx

/**
 * {
 *   "access_token":"ACCESS_TOKEN",
 *   "expires_in":7200,
 *   "refresh_token":"REFRESH_TOKEN",
 *   "openid":"OPENID",
 *   "scope":"SCOPE",
 *   "is_snapshotuser": 1,
 *   "unionid": "UNIONID"
 * }
 */
data class WxH5AccessTokenVo(
    val access_token: String = "",
    val expires_in: Int = 0,
    val refresh_token: String = "",
    val openid: String = "",
    val scope: String = "",
    val is_snapshotuser: String = "",
    val unionid: String = "",
    val errcode: String = "",
    val errmsg: String = ""
)
