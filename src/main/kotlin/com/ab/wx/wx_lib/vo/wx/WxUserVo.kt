package com.ab.wx.wx_lib.vo.wx

/**
 * {
 *   "openid": "OPENID",
 *   "nickname": NICKNAME,
 *   "sex": 1,
 *   "province":"PROVINCE",
 *   "city":"CITY",
 *   "country":"COUNTRY",
 *   "headimgurl":"https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46",
 *   "privilege":[ "PRIVILEGE1" "PRIVILEGE2"     ],
 *   "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
 * }
 */
data class WxUserVo(
    val openid: String = "",
    val nickname: String = "",
    val sex: Int = 0,
    val province: String = "",
    val city: String = "",
    val country: String = "",
    val headimgurl: String = "",
    val privilege: List<String> = arrayListOf(),
    val unionid: String = "",
    val errcode: String = "",
    val errmsg: String = ""
)
