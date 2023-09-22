package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.const.WxConst
import com.ab.wx.wx_lib.dto.miniapp.MiniappMsgDto
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
    val entity = HttpEntity(map, getHeaders())
    val res = restTemplate.postForObject(url, entity, PhoneNumberVo::class.java)
    logger("获取手机号结果:${res}")
    return res
}

/**
 * 订阅消息
 */
fun subscriptMsg(dto: MiniappMsgDto): java.util.HashMap<*, *>? {
    val url = """
            https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=${WxConst.miniAppToken}
        """.trimIndent()
    val entity = HttpEntity(dto, getHeaders())
    return restTemplate.postForObject(url, entity, HashMap::class.java)
}