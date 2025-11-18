package com.ab.wx.wx_lib.vo.pay

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

/**
 * 分账动账通知解密后的数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProfitSharingNotifyVo(
    /**
     * 服务商模式分账发起商户
     */
    val sp_mchid: String = "",
    /**
     * 服务商模式分账出资商户
     */
    val sub_mchid: String = "",
    /**
     * 微信支付订单号
     */
    val transaction_id: String = "",
    /**
     * 微信分账/回退单号
     */
    val order_id: String = "",
    /**
     * 分账方系统内部的分账/回退单号
     */
    val out_order_no: String = "",
    /**
     * 分账接收方对象
     */
    val receiver: ProfitSharingReceiver = ProfitSharingReceiver(),
    /**
     * 成功时间，遵循rfc3339标准格式
     */
    val success_time: String = ""
) : Serializable

/**
 * 分账接收方对象
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProfitSharingReceiver(
    /**
     * 分账接收方类型
     * MERCHANT_ID: 商户
     * PERSONAL_OPENID: 个人(通过openid)
     * PERSONAL_SUB_OPENID: 个人(通过sub_openid)
     */
    val type: String = "",
    /**
     * 分账接收方账号
     * 类型是MERCHANT_ID时，是商户号
     * 类型是PERSONAL_OPENID时，是个人openid
     * 类型是PERSONAL_SUB_OPENID时，是个人sub_openid
     */
    val receiver_account: String = "",
    /**
     * 分账个人姓名
     * 分账接收方类型为PERSONAL_OPENID或PERSONAL_SUB_OPENID时必填
     * 分账接收方类型为MERCHANT_ID时无需填写
     */
    val receiver_name: String = "",
    /**
     * 分账金额
     */
    val amount: Int = 0,
    /**
     * 分账描述
     */
    val description: String = "",
    /**
     * 分账结果
     * SUCCESS: 分账成功
     * CLOSED: 已关闭
     * PENDING: 处理中
     */
    val result: String = "",
    /**
     * 分账失败原因
     * ACCOUNT_ABNORMAL: 分账接收方账户异常
     * TIME_OUT: 超时
     * RECEIVER_REAL_NAME_NOT_VERIFIED: 分账接收方未实名
     * NO_RELATION: 分账关系不正确
     * RECEIVER_NOT_FOUND: 分账接收方不存在
     * OVERDUE_CLOSE: 超时关单
     * SYSTEM_ERROR: 系统错误
     */
    val fail_reason: String = "",
    /**
     * 分账明细单号
     */
    val detail_id: String = ""
) : Serializable