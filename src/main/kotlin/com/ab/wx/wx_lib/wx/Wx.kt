package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.vo.WxTicket
import com.ab.wx.wx_lib.vo.WxToken
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.util.*

class Wx(wxConfigProperties: WxConfigProperties) {
    private val appId = wxConfigProperties.appId
    private val appSec = wxConfigProperties.appSec
    private var callbackUrl = ""
    private var token = ""
    private var ticket = ""

    private val getTokenUrl =
        "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${appId}&secret=${appSec}"

    private val getTicketUrl =
        "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=${token}&type=jsapi"

    init {
        genToken()
    }

    fun genToken() {
        val restTemplate = RestTemplate()
        val res = restTemplate.getForObject(getTokenUrl, WxToken::class.java)
        res?.let {
            this.token = res.access_token
            restTemplate.getForObject(getTicketUrl, WxTicket::class.java)?.let {
                this.ticket = it.ticket
            }
        }
    }


    fun createSign(jsapi_ticket: String, url: String): HashMap<String, String> {
        val map = hashMapOf<String, String>()
        val nonce_str = create_nonce_str()
        val timestamp = create_timestamp()
        val tmpStr = "jsapi_ticket=$jsapi_ticket&noncestr=$nonce_str&timestamp=$timestamp&url=$url"
        val crypt = MessageDigest.getInstance("SHA-1")
        crypt.reset()
        crypt.update(tmpStr.toByteArray(charset("UTF-8")))
        val signature = byteToHex(crypt.digest())
        map["url"] = url
        map["appId"] = appId
        map["jsapi_ticket"] = jsapi_ticket
        map["nonceStr"] = nonce_str
        map["timestamp"] = timestamp
        map["signature"] = signature
        map["token"] = token
        return map
    }

    fun check(signature: String, timestamp: String, nonce: String, echostr: String): String {
        val list = arrayOf(token, timestamp, nonce)
        Arrays.sort(list)
        val sb = StringBuffer()
        list.forEach { it -> sb.append(it) }
        val str = sha1(sb.toString())
        if (str == signature)
            return echostr
        return ""
    }


}