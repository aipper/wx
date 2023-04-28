package com.ab.wx.wx_lib.vo.pay

import com.ab.wx.wx_lib.dto.pay.RefundsGoods

/**
 * 退款返回数据
 * {
 *   "refund_id": "50000000382019052709732678859",
 *   "out_refund_no": "1217752501201407033233368018",
 *   "transaction_id": "1217752501201407033233368018",
 *   "out_trade_no": "1217752501201407033233368018",
 *   "channel": "ORIGINAL",
 *   "user_received_account": "招商银行信用卡0403",
 *   "success_time": "2020-12-01T16:18:12+08:00",
 *   "create_time": "2020-12-01T16:18:12+08:00",
 *   "status": "SUCCESS",
 *   "funds_account": "UNSETTLED",
 *   "amount": {
 *     "total": 100,
 *     "refund": 100,
 *     "from": [
 *       {
 *         "account": "AVAILABLE",
 *         "amount": 444
 *       }
 *     ],
 *     "payer_total": 90,
 *     "payer_refund": 90,
 *     "settlement_refund": 100,
 *     "settlement_total": 100,
 *     "discount_refund": 10,
 *     "currency": "CNY"
 *   },
 *   "promotion_detail": [
 *     {
 *       "promotion_id": "109519",
 *       "scope": "SINGLE",
 *       "type": "DISCOUNT",
 *       "amount": 5,
 *       "refund_amount": 100,
 *       "goods_detail": [
 * 	    {
 * 			"merchant_goods_id": "1217752501201407033233368018",
 * 			"wechatpay_goods_id": "1001",
 * 			"goods_name": "iPhone6s 16G",
 * 			"unit_price": 528800,
 * 			"refund_amount": 528800,
 * 			"refund_quantity": 1
 * 		}
 *       ]
 *     }
 *   ]
 * }
 *
 */
data class RefundsVo(
    val refund_id: String,
    val out_refund_no: String,
    val transaction_id: String,
    val out_trade_no: String,
    val channel: String,
    val user_received_account: String,
    val success_time: String,
    val create_time: String,
    val status: String,
    val funds_account: String,
    val amount: RefundsAmountVo,
    val promotion_detail: List<RefundsPromotionDetail>?

)

data class RefundsAmountFromVo(
    val account: String, val amount: Int
)

data class RefundsAmountVo(
    /**
     * 订单金额
     */
    val total: Int,
    /**
     * 退款金额
     */
    val refund: Int,
    /**
     *
     */
    val from: List<RefundsAmountFromVo>?,
    /**
     * 用户支付金额
     */
    val payer_total: Int,
    /**
     * 用户退款金额
     */
    val payer_refund: Int,
    /**
     * 应结退款金额
     */
    val settlement_refund: Int,
    /**
     * 应结订单金额
     */
    val settlement_total: Int,
    /**
     * 优惠退款金额
     */
    val discount_refund: Int,
    /**
     * 退款币种
     */
    val currency: String = "CNY",
    /**
     * 手续费退款金额
     */
    val refund_fee: Int = 0
)

data class RefundsPromotionDetail(
    val promotion_id: String,
    val scope: String,
    val type: String,
    val amount: Int,
    val refund_amount: Int,
    val goods_detail: List<RefundsGoods>?
)
