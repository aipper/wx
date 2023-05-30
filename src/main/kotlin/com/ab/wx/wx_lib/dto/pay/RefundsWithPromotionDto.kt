package com.ab.wx.wx_lib.dto.pay

import com.ab.wx.wx_lib.enums.PromotionScopeEnum
import com.ab.wx.wx_lib.enums.PromotionTypeEnum
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 退款同时退使用的的优惠券
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class RefundsWithPromotionDto(
    val orderId: String,
    val refundsOrderId: String,
    val refundsMoney: Int = 0,
    val totalMoney: Int = 0,
//    val promotion: List<RefundsPromotion> = arrayListOf()
)

data class RefundsPromotion(
    val promotion_id: String = "",
    val scope: String = PromotionScopeEnum.GLOBAL.code,
    val type: String = PromotionTypeEnum.DISCOUNT.code,
    val amount: Int = 0,
    val refund_amount: Int = 0
)