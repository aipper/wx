package com.ab.wx.wx_lib.vo.pay

/**
 * 查询最大分账比例响应VO
 * 
 * @param maxRatio 最大分账比例，单位万分比，比如2000表示20%
 */
data class QueryMaxRatioVo(
    val maxRatio: Int
)