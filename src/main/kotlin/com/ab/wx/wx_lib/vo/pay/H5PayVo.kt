package com.ab.wx.wx_lib.vo.pay

data class H5PayVo(
    val id: String = "",
    val create_time: String = "",
    val resource_type: String = "",
    val event_type: String = "",
    val summary: String = "",
    val resource: H5PayResource = H5PayResource()
)

data class H5PayResource(
    val original_type: String = "",
    val algorithm: String = "",
    val ciphertext: String = "",
    val associated_data: String = "",
    val nonce: String = ""
)