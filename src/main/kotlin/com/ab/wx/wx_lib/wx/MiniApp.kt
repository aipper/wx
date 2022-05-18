package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.vo.miniapp.Code2SessionVo
import org.springframework.web.client.RestTemplate

class MiniApp(wxConfigProperties: WxConfigProperties) {
    private val miniAppId = wxConfigProperties.miniAppId
    private val miniAppSec =wxConfigProperties.miniAppSec
    private var restTemplate: RestTemplate = RestTemplate()

    fun getCode2Session(code: String): Code2SessionVo? {
        val code2sessionUrl = """
            https://api.weixin.qq.com/sns/jscode2session?appid=${miniAppId}&secret=${miniAppSec}&js_code=${code}&grant_type=authorization_code
        """.trimIndent()
        return restTemplate.getForObject(code2sessionUrl, Code2SessionVo::class.java)
    }
}