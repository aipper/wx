package com.ab.wx.wx_lib.vo.pay

/**
 * {
 *     "mchid": "1900000100",
 *     "transaction_id": "1008450740201411110005820873",
 *     "out_trade_no": "20150806125346",
 *     "refund_id": "50200207182018070300011301001",
 *     "out_refund_no": "7752501201407033233368018",
 *     "refund_status": "SUCCESS",
 *     "success_time": "2018-06-08T10:34:56+08:00",
 *     "user_received_account": "招商银行信用卡0403",
 *     "amount" : {
 *         "total": 999,
 *         "refund": 999,
 *         "payer_total": 999,
 *         "payer_refund": 999
 *     }
 * }
 */
data class H5RefundsDecodeVo(
    val mchid: String,
    val transaction_id: String,
    val out_trade_no: String,
    val refund_id: String,
    val out_refund_no: String,
    val refund_status: String,
    val success_time: String,
    val user_received_account: String,
    val amount: DecodeAmountVo,
)

data class DecodeAmountVo(
    val total: Int, val refund: Int, val payer_total: Int, val payer_refund: Int
)