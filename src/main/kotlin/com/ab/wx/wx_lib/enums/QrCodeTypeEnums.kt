package com.ab.wx.wx_lib.enums

enum class QrCodeTypeEnums(val code: String, val desc: String) {
    PERMANENT("QR_LIMIT_STR_SCENE", "永久二维码"), EXPIRED("QR_STR_SCENE", "临时二维码")
}