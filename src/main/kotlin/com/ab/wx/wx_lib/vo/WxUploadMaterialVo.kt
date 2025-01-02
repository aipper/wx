package com.ab.wx.wx_lib.vo

import java.io.Serializable

data class WxUploadMaterialVo(
    val media_id: String = "", val url: String = "", val item: Any? = null
) : Serializable