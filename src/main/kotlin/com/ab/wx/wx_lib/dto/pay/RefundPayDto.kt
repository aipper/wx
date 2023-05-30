package com.ab.wx.wx_lib.dto.pay

/**
 * 退款
 * {
 *   "transaction_id": "1217752501201407033233368018",
 *   "out_refund_no": "1217752501201407033233368018",
 *   "reason": "商品已售完",
 *   "notify_url": "https://weixin.qq.com",
 *   "funds_account": "AVAILABLE",
 *   "amount": {
 *     "refund": 888,
 *     "from": [
 *       {
 *         "account": "AVAILABLE",
 *         "amount": 444
 *       }
 *     ],
 *     "total": 888,
 *     "currency": "CNY"
 *   },
 *   "goods_detail": [
 *     {
 *       "merchant_goods_id": "1217752501201407033233368018",
 *       "wechatpay_goods_id": "1001",
 *       "goods_name": "iPhone6s 16G",
 *       "unit_price": 528800,
 *       "refund_amount": 528800,
 *       "refund_quantity": 1
 *     }
 *   ]
 * }
 */
data class RefundPayDto(
    /**
     * 微信支付订单号
     */
    val transaction_id: String? = null,
    /**
     * 商户订单号 同微信支付订单号 二选一
     */
    val out_trade_no: String? = null,
    /**
     * 商户退款单号
     */
    val out_refund_no: String = "",
    /**
     * 退款原因
     */
    val reason: String? = null,
    /**
     * 退款回调
     */
    val notify_url: String? = null,
    /**
     * 退款资金来源
     */
    val funds_account: String? = null,
    /**
     * 金额信息
     */
    val amount: RefundsAmount? = null,
    /**
     * 商品信息
     */
    val goods_detail: List<RefundsGoods>? = null,
//    /**
//     * 优惠退款
//     */
//    val promotion_detail: List<RefundsPromotion> = arrayListOf()
)

data class RefundsAmount(
    val refund: Int = 0, val from: List<RefundsFrom> = arrayListOf(), val total: Int = 0, val currency: String = "CNY"
)

data class RefundsFrom(
    val account: String = "", val amount: Int = 0
)

data class RefundsGoods(
    /**
     * 商户侧商品编码
     */
    val merchant_goods_id: String = "",
    /**
     * 微信支付商品编码
     */
    val wechatpay_goods_id: String? = null,
    /**
     *  商品名称
     */
    val goods_name: String? = null,
    /**
     * 商品单价
     */
    val unit_price: Int = 0,
    /**
     * 退款金额
     */
    val refund_amount: Int = 0,
    /**
     * 退货数量
     */
    val refund_quantity: Int = 0
)