package com.ab.wx.wx_lib.dto.qrcode

import com.ab.wx.wx_lib.enums.QrCodeTypeEnums

/**
 * 永久二维码参数
 */
data class PermanentQrCodeDto(
    val action_name: String = QrCodeTypeEnums.PERMANENT.code,
    val action_info: QrCodeActionInfoDto = QrCodeActionInfoDto()
)
