package com.ab.wx.wx_lib.dto.pay

/**
 * 查询最大分账比例DTO
 * 
 * 用于查询特约商户设置的允许服务商分账的最大比例
 * 
 * @param subMchid 子商户号，即分账的出资商户号
 */
data class QueryMaxRatioDto(
    val subMchid: String
)