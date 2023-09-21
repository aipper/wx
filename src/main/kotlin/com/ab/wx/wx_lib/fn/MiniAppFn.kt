package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.const.WxConst
import com.ab.wx.wx_lib.vo.miniapp.PhoneNumberVo
import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate


private val restTemplate: RestTemplate = getRestTemplate()

/**
 * 获取手机号
 */
fun getPhoneNumber(code: String): PhoneNumberVo? {
    val url = """
            https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=${WxConst.miniAppToken}
        """.trimIndent()
    val map = hashMapOf<String, String>()
    map["code"] = code
    logger("获取手机号url:${url}")
    val entity = HttpEntity(map)
    val res = restTemplate.postForObject(url, entity, PhoneNumberVo::class.java)
    logger("获取手机号结果:${res}")
    return res
}