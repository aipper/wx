package com.ab.wx.wx_lib.dto.qrcode

data class QrCodeActionInfoDto(
    val scene: QrCodeScene = QrCodeScene()
)

data class QrCodeScene(
    val scene_str: String = ""
)
