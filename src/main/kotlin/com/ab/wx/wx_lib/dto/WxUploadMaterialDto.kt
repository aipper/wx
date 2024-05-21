package com.ab.wx.wx_lib.dto

import java.io.File


/**
 * 素材上传
 */

data class WxUploadMaterialDto(
    val file: File, val type: String = WxUploadMaterialType.IMAGE.type
)

enum class WxUploadMaterialType(val type: String) {
    IMAGE("image"), VOICE("voice"), VIDEO("video"), THUMB("thumb")
}
