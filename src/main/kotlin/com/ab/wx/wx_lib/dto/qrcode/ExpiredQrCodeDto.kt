package com.ab.wx.wx_lib.dto.qrcode

import com.ab.wx.wx_lib.enums.QrCodeTypeEnums

data class ExpiredQrCodeDto(
    val expire_seconds: Long = 2592000,
    val action_name: String = QrCodeTypeEnums.EXPIRED.code,
    val action_info: QrCodeActionInfoDto = QrCodeActionInfoDto()
)
