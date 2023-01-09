package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.dto.miniapp.AppUniformMsgSendDto
import com.ab.wx.wx_lib.fn.getRestTemplate
import com.ab.wx.wx_lib.vo.miniapp.AppAccessTokenVo
import com.ab.wx.wx_lib.vo.miniapp.Code2SessionVo
import com.ab.wx.wx_lib.vo.miniapp.PhoneNumberVo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate

class MiniApp(val miniAppId: String,val  miniAppSec: String) {
    private val logger = LoggerFactory.getLogger(MiniApp::class.java)
    private val restTemplate: RestTemplate = getRestTemplate()
    private var accessToken:String=""

    fun getCode2Session(code: String): Code2SessionVo? {
        val code2sessionUrl = """
            https://api.weixin.qq.com/sns/jscode2session?appid=${miniAppId}&secret=${miniAppSec}&js_code=${code}&grant_type=authorization_code
        """.trimIndent()
        return restTemplate.getForObject(code2sessionUrl, Code2SessionVo::class.java)
    }

    /**
     *  生成接口调用凭证
     */
    fun genAccessToken(): String {
        val url = """
            https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${miniAppId}&secret=$miniAppSec
        """.trimIndent()
        val res = restTemplate.getForObject(url, AppAccessTokenVo::class.java)
        res?.let {
            accessToken = res.access_token
            return accessToken
        }
        return ""
    }

    /**
     *  获取生成的accessToken
     */
    fun getAccessToken(): String {
       return accessToken
    }

    /**
     * 设置accessToken
     */
    fun setAccessToken(token: String) {
        this.accessToken = token
    }

    /**
     * 统一消息回复
     */
    fun uniformMsgSend(dto: AppUniformMsgSendDto) {
        val url = """
            https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=${accessToken}
        """.trimIndent()
    }

    /**
     * 获取手机号
     */
    fun getPhoneNumber(code: String): PhoneNumberVo? {
        val url = """
            https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=${accessToken}
        """.trimIndent()
        val map = hashMapOf<String, String>()
        map["code"] = code
        val entity = HttpEntity(map)
        val res = restTemplate.postForObject(url, entity, PhoneNumberVo::class.java)
        logger.info("res:$res")
        return res
    }

    fun setExpireTime(string: String) {

    }
}