package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.const.WxConst
import com.ab.wx.wx_lib.dto.WxCreateMenuDto
import com.ab.wx.wx_lib.dto.WxSendTemplateDto
import com.ab.wx.wx_lib.dto.qrcode.ExpiredQrCodeDto
import com.ab.wx.wx_lib.dto.qrcode.PermanentQrCodeDto
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.vo.WxTicket
import com.ab.wx.wx_lib.vo.WxToken
import com.ab.wx.wx_lib.vo.wx.WxCreateMenuVo
import com.ab.wx.wx_lib.vo.wx.WxQrCodeVo
import com.ab.wx.wx_lib.vo.wx.WxTemplateVo
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.util.*

class Wx(wxConfigProperties: WxConfigProperties) {
    private val logger = LoggerFactory.getLogger(Wx::class.java)
    private val appId = wxConfigProperties.appId
    private val appSec = wxConfigProperties.appSec
    private val wxToken = wxConfigProperties.wxToken
    private var callbackUrl = ""
    private val restTemplate = getRestTemplate()
    private val mapper = ObjectMapper()

    private val getTokenUrl =
        "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${appId}&secret=${appSec}"

    private fun getTicketUrl(token: String?) =
        "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=${token}&type=jsapi"


    /**
     * 创建菜单
     */
    private fun createMenuUrl(token: String) = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=${token}"

    /**
     * 模板消息
     */
    private fun templateUrl(token: String?) =
        "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=${token}"

    private fun qrCodeUrl(token: String?) = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=${token}"

    /**
     * 获取token
     */
    fun genToken() {
        val restTemplate = RestTemplate()
        val res = restTemplate.getForObject(getTokenUrl, WxToken::class.java)
        res?.let {
            WxConst.accessToken = res.access_token
            getTicket(res.access_token)
        }
    }

    fun getTicket(token: String?) {
        restTemplate.getForObject(getTicketUrl(token), WxTicket::class.java)?.let {
            WxConst.ticket = it.ticket
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
        map["token"] = wxToken
        return map
    }

    fun check(signature: String, timestamp: String, nonce: String, echostr: String): String {
        val list = arrayOf(wxToken, timestamp, nonce)
        Arrays.sort(list)
        val sb = StringBuffer()
        list.forEach { sb.append(it) }
        val str = sha1(sb.toString())
        if (str == signature) return echostr
        return ""
    }

    fun createMenu(dto: WxCreateMenuDto): WxCreateMenuVo? {
        val entity = HttpEntity<WxCreateMenuDto>(dto)
        return restTemplate.postForObject(createMenuUrl(WxConst.accessToken), entity, WxCreateMenuVo::class.java)
    }


    fun sendTemplate(dto: WxSendTemplateDto): WxTemplateVo? {
        val entity = HttpEntity(dto)
        return restTemplate.postForObject(templateUrl(WxConst.accessToken), entity, WxTemplateVo::class.java)
    }


    private fun getHeaders(): HttpHeaders {
        val header = HttpHeaders()
        header.contentType = MediaType.APPLICATION_JSON
        return header
    }

    /**
     * 生成永久二维码
     */
    fun genPermanentQrCode(dto: PermanentQrCodeDto): WxQrCodeVo? {
        val entity = HttpEntity(dto, getHeaders())
        logger.info("qrCodeUrl:${qrCodeUrl(WxConst.accessToken)}")
        return restTemplate.postForObject(qrCodeUrl(WxConst.accessToken), entity, WxQrCodeVo::class.java)
    }

    /**
     * 生成临时二维码
     */
    fun genExpiredQrCode(dto: ExpiredQrCodeDto): WxQrCodeVo? {
        val entity = HttpEntity<ExpiredQrCodeDto>(dto, getHeaders())
        return restTemplate.postForObject(qrCodeUrl(WxConst.accessToken), entity, WxQrCodeVo::class.java)
    }
}