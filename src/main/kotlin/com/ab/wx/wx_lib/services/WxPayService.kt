package com.ab.wx.wx_lib.services

import com.ab.wx.wx_lib.dto.pay.SimplePayDto
import com.ab.wx.wx_lib.dto.pay.SimpleRefundsDto
import com.ab.wx.wx_lib.vo.pay.H5PayDecodeVo
import com.ab.wx.wx_lib.vo.pay.H5RefundsDecodeVo
import com.ab.wx.wx_lib.vo.pay.JsApiPayRes
import jakarta.servlet.http.HttpServletRequest

/**
 * 微信支付接口抽象
 * 定义微信支付核心功能接口
 */
interface WxPayService {
    /**
     * 获取JSApi支付参数
     * @param dto 支付请求数据
     * @return JSApi支付参数
     */
    fun createJsApiPay(dto: SimplePayDto): Result<JsApiPayRes>

    /**
     * 获取小程序支付参数
     * @param dto 支付请求数据
     * @return JSApi支付参数
     */
    fun createMiniAppPay(dto: SimplePayDto): Result<JsApiPayRes>

    /**
     * 处理支付回调
     * @param request HTTP请求
     * @return 支付回调结果
     */
    fun handlePayCallback(request: HttpServletRequest): Result<H5PayDecodeVo>

    /**
     * 申请退款
     * @param dto 退款请求数据
     * @return 退款结果
     */
    fun refund(dto: SimpleRefundsDto): Result<RefundResult>

    /**
     * 处理退款回调
     * @param request HTTP请求
     * @return 退款回调结果
     */
    fun handleRefundCallback(request: HttpServletRequest): Result<H5RefundsDecodeVo>
}
