package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxPayConfigProperties
import com.ab.wx.wx_lib.dto.pay.JsApiPayDto
import com.ab.wx.wx_lib.fn.getHeaders
import com.ab.wx.wx_lib.fn.getRestTemplate
import com.ab.wx.wx_lib.vo.pay.JsApiPayVo
import org.springframework.http.HttpEntity

class WxPay(wxPayConfigProperties: WxPayConfigProperties) {
    private val mchId = wxPayConfigProperties.mchid

    private val restTemplate = getRestTemplate()

    private val jsApiPayUrl = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi"


    fun genJsApiPay(dto: JsApiPayDto): JsApiPayVo? {
        dto.mchid = mchId
        val entity = HttpEntity(dto, getHeaders())
        return restTemplate.postForObject(jsApiPayUrl, entity, JsApiPayVo::class.java)
    }
}