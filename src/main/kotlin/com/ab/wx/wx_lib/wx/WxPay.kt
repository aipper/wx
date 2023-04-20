package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.dto.pay.JsApiPayAmountDto
import com.ab.wx.wx_lib.dto.pay.JsApiPayDto
import com.ab.wx.wx_lib.dto.pay.JsApiPayerDto
import com.ab.wx.wx_lib.dto.pay.SimplePayDto
import com.ab.wx.wx_lib.fn.getHeaders
import com.ab.wx.wx_lib.fn.getRestTemplate
import com.ab.wx.wx_lib.vo.pay.JsApiPayVo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity

class WxPay(wxConfigProperties: WxConfigProperties) {
    private val logger = LoggerFactory.getLogger(WxPay::class.java)
    private val mchId = wxConfigProperties.pay?.mchid
    private val notifyUrl = wxConfigProperties.pay?.notifyUrl

    private val appId = wxConfigProperties.appId

    private val restTemplate = getRestTemplate()

    private val jsApiPayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi"


    private val h5PayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5"


    fun genSimplePay(dto: SimplePayDto): JsApiPayVo? {
        mchId?.let {
            val payDto = JsApiPayDto(
                appid = appId,
                mchid = mchId,
                description = dto.description,
                out_trade_no = dto.orderNo,
                notify_url = if (dto.notifyUrl.isNullOrBlank()) "$notifyUrl" else dto.notifyUrl,
                amount = JsApiPayAmountDto(total = dto.amount),
                payer = JsApiPayerDto(dto.payOpenid)
            )
            return genJsApiPay(payDto)
        }
        return null
    }

    private fun genJsApiPay(dto: JsApiPayDto): JsApiPayVo? {
        val entity = HttpEntity(dto, getHeaders())
        logger.info("genJsApiPay:$dto")
        return restTemplate.postForObject(jsApiPayUrl, entity, JsApiPayVo::class.java)
    }


    fun genH5Pay() {

    }
}