package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.const.WxConst
import com.ab.wx.wx_lib.dto.miniapp.MiniappMsgDto
import com.ab.wx.wx_lib.vo.miniapp.PhoneNumberVo
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate


internal val restTemplate: RestTemplate = getRestTemplate()

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
    val entity = HttpEntity(map, getHeaders(map))
    val res = restTemplate.postForObject(url, entity, String::class.java)
    logger("获取手机号结果String:${res}")
    if (res != null) {
        val result = getMapper().readValue(res, PhoneNumberVo::class.java)
        return result
    } else {
        return null
    }

}

/**
 * 订阅消息
 */
fun subscriptMsg(dto: MiniappMsgDto): java.util.HashMap<*, *>? {
    val url = """
            https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=${WxConst.miniAppToken}
        """.trimIndent()
    val entity = HttpEntity(dto, getHeaders())
    logger("subscriptMsg:$dto")
    return restTemplate.postForObject(url, entity, HashMap::class.java)
}