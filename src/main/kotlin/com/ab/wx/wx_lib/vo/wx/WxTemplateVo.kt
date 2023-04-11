package com.ab.wx.wx_lib.vo.wx

/**
 * {
 *
 * 	"errcode":0,
 *
 * 	"errmsg":"ok",
 *
 * 	"msgid":200228332
 *
 * }
 */
data class WxTemplateVo(
    val errcode: Int = 0, val errmsg: String = "", val msgid: Any? = null
)
