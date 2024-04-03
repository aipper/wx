package com.ab.wx.wx_lib.vo.pay

import com.ab.wx.wx_lib.enums.TransMoneyStatusEnums
import com.ab.wx.wx_lib.fn.getMapper

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
/**
 *  at [Source: (String)"{"mchid":"1638537256",
 *  "out_trade_no":"1654026214064640002",
 *  "transaction_id":"4200001840202305046776453156",
 *  "out_refund_no":"1654045066335981570",
 *  "refund_id":"50302405472023050434079463166",
 *  "refund_status":"SUCCESS","
 *  success_time":"2023-05-04T16:47:37+08:00",
 *  "amount":{"total":2,"refund":1,"payer_total":2,"payer_refund":1},
 *  "user_received_account":"交通银行信用卡9558"}"; line: 1, column: 2]
 *
 */

data class H5RefundsDecodeVo(
    val mchid: String = "",
    val transaction_id: String = "",
    val out_trade_no: String = "",
    val refund_id: String = "",
    val out_refund_no: String = "",
    val refund_status: String = "",
    val success_time: String = "",
    val user_received_account: String = "",
    val amount: DecodeAmountVo = DecodeAmountVo(),
)

data class DecodeAmountVo(
    val total: Int = 0, val refund: Int = 0, val payer_total: Int = 0, val payer_refund: Int = 0
)


/**
 * {
 *     "out_batch_no": "bfatestnotify000033",
 *     "batch_id": "131000007026709999520922023081519403795655",
 *     "batch_status": "FINISHED",
 *     "total_num": 2,
 *     "total_amount": 200,
 *     "success_amount": 100,
 *     "success_num": 1,
 *     "fail_amount": 100,
 *     "fail_num": 1,
 *     "mchid": "2483775951",
 *     "update_time": "2023-08-15T20:33:22+08:00"
 * }
 * 微信转账零钱
 */
data class TransferCallbackVo(
    val out_batch_no: String = "",
    val batch_id: String = "",
    val batch_status: TransMoneyStatusEnums = TransMoneyStatusEnums.ACCEPTED,
    val total_num: Int = 0,
    val total_amount: Int = 0,
    val success_amount: Int = 0,
    val success_num: Int = 0,
    val fail_amount: Int = 0,
    val fail_num: Int = 0,
    val mchid: String = "",
    val update_time: String = ""
)
