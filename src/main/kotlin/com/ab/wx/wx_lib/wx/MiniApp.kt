package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.const.WxConst
import com.ab.wx.wx_lib.dto.miniapp.AppUniformMsgSendDto
import com.ab.wx.wx_lib.dto.miniapp.MiniappMsgDto
import com.ab.wx.wx_lib.exception.WxException
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.vo.miniapp.AppAccessTokenVo
import com.ab.wx.wx_lib.vo.miniapp.Code2SessionVo
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.body
import java.util.Arrays

class MiniApp(wxConfigProperties: WxConfigProperties) {
    //    private val restTemplate: RestTemplate = getRestTemplate()
    private val restClient = getRestClient()
    private val miniAppId = wxConfigProperties.miniAppId
    private val miniAppSec = wxConfigProperties.miniAppSec
    private val miniAppToken = wxConfigProperties.miniAppToken
    private var accessToken: String = ""

    fun getCode2Session(code: String): Code2SessionVo? {
        val code2sessionUrl = """
            https://api.weixin.qq.com/sns/jscode2session?appid=${miniAppId}&secret=${miniAppSec}&js_code=${code}&grant_type=authorization_code
        """.trimIndent()
//        return restTemplate.getForObject(code2sessionUrl, Code2SessionVo::class.java)
        val res = restClient.get().uri(code2sessionUrl).retrieve().body<String>()
        res?.let {
            return getMapper().readValue(it, Code2SessionVo::class.java)
        }
        return null
    }

    /**
     *  生成接口调用凭证
     */
    fun genAccessToken(): String {
        val url = """
            https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${miniAppId}&secret=$miniAppSec
        """.trimIndent()
//        val res = restTemplate.getForObject(url, AppAccessTokenVo::class.java)
//        logger("请求小程序接口凭证:$url")
//        res?.let {
//            accessToken = res.access_token
//            WxConst.miniAppToken = res.access_token
//            logger("获取小程序token为:${WxConst.miniAppToken}")
//            return accessToken
//        }
//        return ""
        val res = restClient.get().uri(url).retrieve().body(AppAccessTokenVo::class.java)
        if (res != null) {
            accessToken = res.access_token
            WxConst.miniAppToken = res.access_token
            logger("获取小程序token为:${WxConst.miniAppToken}")
            return accessToken
        }
        return ""
    }

    /**
     *  获取生成的accessToken
     */
    fun getAccessToken(): String {
        return WxConst.accessToken
    }

    /**
     * 设置accessToken
     */
    fun setAccessToken(token: String) {
//        this.accessToken = token
        WxConst.miniAppToken = token
    }

    /**
     * 统一消息回复
     */
    fun uniformMsgSend(dto: AppUniformMsgSendDto) {
        val url = """
            https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=${WxConst.miniAppToken}
        """.trimIndent()
    }

    /**
     * 消息验证
     */
    fun check(signature: String, timestamp: String, nonce: String, echostr: String): String {
        if (miniAppToken.isEmpty()){
            throw WxException("小程序token为空")
        }
        val list = arrayOf(miniAppToken, timestamp, nonce)
        Arrays.sort(list)
        val sb = StringBuffer()
        list.forEach { sb.append(it) }
        val str = sha1(sb.toString())
        if (str == signature) return echostr
        return ""
    }

}
