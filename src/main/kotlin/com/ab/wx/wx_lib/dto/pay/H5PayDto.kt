package com.ab.wx.wx_lib.dto.pay

/**
 * H5 下单请求
 */
data class H5PayDto(
    val appid: String,
    val mchid: String,
    val description: String,
    val out_trade_no: String,
    val notify_url: String,
    val amount: JsApiPayAmountDto,
    val scene_info: H5SceneInfoDto
)

data class H5SceneInfoDto(
    val payer_client_ip: String,
    val h5_info: H5InfoDto
)

data class H5InfoDto(
    val type: String = "Wap",
    val app_name: String? = null,
    val app_url: String? = null,
    val bundle_id: String? = null,
    val package_name: String? = null,
    val wap_url: String? = null,
    val wap_name: String? = null
)
