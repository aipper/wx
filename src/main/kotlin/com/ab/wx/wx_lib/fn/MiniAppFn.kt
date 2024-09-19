package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.const.WxConst
import com.ab.wx.wx_lib.dto.miniapp.MiniappMsgDto
import com.ab.wx.wx_lib.dto.miniapp.PhoneNumberDto
import com.ab.wx.wx_lib.vo.miniapp.PhoneNumberVo
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.function.RequestPredicates.contentType


//internal val restTemplate: RestTemplate = getRestTemplate()
internal val restClient = getRestClient()


/**
 * 获取手机号
 */
fun getPhoneNumber(code: String): PhoneNumberVo? {

    val url = """
            https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=${WxConst.miniAppToken}
        """.trimIndent()
    val dto = PhoneNumberDto(code)
    return restClient.post().uri(url).body(dto).contentLength(getContentLength(dto))
        .contentType(MediaType.APPLICATION_JSON).retrieve().toEntity(PhoneNumberVo::class.java).body

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
//    return restTemplate.postForObject(url, entity, HashMap::class.java)
    return restClient.post().uri(url).body(dto).contentLength(getContentLength(dto)).contentType(MediaType.APPLICATION_JSON).retrieve()
        .toEntity(java.util.HashMap::class.java).body
}