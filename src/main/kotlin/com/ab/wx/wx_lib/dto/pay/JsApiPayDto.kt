package com.ab.wx.wx_lib.dto.pay

data class JsApiPayDto(
    val appid: String = "",
    val mchid: String = "",
    val description: String = "",
    val out_trade_no: String = "",
    val time_expire: String? = null,
    val attach: String? = null,
    val notify_url: String = "",
    val goods_tag: String? = null,
    val support_fapiao: Boolean = false,
    val amount: JsApiPayAmountDto = JsApiPayAmountDto(),
    val payer: JsApiPayerDto = JsApiPayerDto(),
    /**
     * 优惠功能
     */
    val detail: JsApiPayDetailDto? = null,

    /**
     * 场景信息
     */
    val scene_info: SceneInfoDto? = null,
    /**
     * 结算信息
     */
    val settle_info: SettleInfoDto = SettleInfoDto()
)

data class JsApiPayAmountDto(
    val total: Int = 0, val currency: String = "CNY"
)

data class JsApiPayerDto(
    val openid: String = ""
)

data class JsApiPayDetailDto(
    /**
     * 订单原价 1、商户侧一张小票订单可能被分多次支付，订单原价用于记录整张小票的交易金额。
     * 2、当订单原价与支付金额不相等，则不享受优惠。
     * 3、该字段主要用于防止同一张小票分多次支付，以享受多次优惠的情况，正常支付订单不必上传此参数。
     */
    val cost_price: Int = 0,
    /**
     * 商家小票ID
     *
     */
    val invoice_id: String = "", val goods_detail: List<goodsDetailDto>? = null
)

data class goodsDetailDto(
    /**
     * 商户侧商品编码 由半角的大小写字母、数字、中划线、下划线中的一种或几种组成。
     */
    val merchant_goods_id: String = "",
    /**
     * 微信支付商品编码 微信支付定义的统一商品编号（没有可不传）
     */
    val wechatpay_goods_id: String? = null,
    /**
     * 商品名称
     */
    val goods_name: String? = null,
    /**
     * 商品数量
     */
    val quantity: Int = 0,
    /**
     * 商品单价
     */
    val unit_price: Int = 0
)

data class SceneInfoDto(
    /**
     * 用户终端IP
     */
    val payer_client_ip: String = "",
    /**
     * 商户端设备号
     */
    val device_id: String = ""
)

data class SettleInfoDto(
    val profit_sharing: Boolean = false
)