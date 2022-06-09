package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.const.MiniAppConst
import com.ab.wx.wx_lib.dto.miniapp.AppUniformMsgSendDto
import com.ab.wx.wx_lib.fn.getRestTemplate
import com.ab.wx.wx_lib.vo.miniapp.AppAccessTokenVo
import com.ab.wx.wx_lib.vo.miniapp.Code2SessionVo
import com.ab.wx.wx_lib.vo.miniapp.PhoneNumberVo
import org.springframework.web.client.RestTemplate

class MiniApp(wxConfigProperties: WxConfigProperties) {
    private val miniAppId = wxConfigProperties.miniAppId
    private val miniAppSec = wxConfigProperties.miniAppSec
    private val restTemplate: RestTemplate = getRestTemplate()

    fun getCode2Session(code: String): Code2SessionVo? {
        val code2sessionUrl = """
            https://api.weixin.qq.com/sns/jscode2session?appid=${miniAppId}&secret=${miniAppSec}&js_code=${code}&grant_type=authorization_code
        """.trimIndent()
        return restTemplate.getForObject(code2sessionUrl, Code2SessionVo::class.java)
    }

    /**
     * 接口调用凭证
     */
    fun getAccessToken() {
        val url = """
            https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${miniAppId}&secret=$miniAppSec
        """.trimIndent()
        val res = restTemplate.getForObject(url, AppAccessTokenVo::class.java)
        res?.let {
            MiniAppConst.accessToken = res.access_token
        }
    }

    /**
     * 统一消息回复
     */
    fun uniformMsgSend(dto: AppUniformMsgSendDto) {
        val url = """
            https://api.weixin.qq.com/cgi-bin/message/wxopen/template/uniform_send?access_token=${MiniAppConst.accessToken}
        """.trimIndent()
    }

    /**
     * 获取手机号
     */
    fun getPhoneNumber(code: String): String? {
        val url = """
            https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=${MiniAppConst.accessToken}&code=${code}
        """.trimIndent()
        val res = restTemplate.getForObject(url, PhoneNumberVo::class.java)
        res?.let {
           return it.phone_info?.purePhoneNumber
        }
        return ""
    }
}