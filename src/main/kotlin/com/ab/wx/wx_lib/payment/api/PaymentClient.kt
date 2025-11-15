package com.ab.wx.wx_lib.payment.api

/**
 * 对外暴露的统一支付客户端接口。不同支付渠道（微信/支付宝）将通过实现该接口来提供订单、退款等操作。
 */
interface PaymentClient {

    val provider: PaymentProvider

    fun createOrder(command: CreateOrderCommand): CreateOrderResult

    fun queryOrder(command: QueryOrderCommand): QueryOrderResult

    fun closeOrder(command: CloseOrderCommand): CloseOrderResult

    fun refund(command: RefundCommand): RefundResult
}

class PaymentClientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
