package com.ab.wx.wx_lib.wx

import com.ab.wx.wx_lib.config.WxConfigProperties
import com.ab.wx.wx_lib.const.WxConst
import com.ab.wx.wx_lib.dto.WxCreateMenuDto
import com.ab.wx.wx_lib.dto.WxSendTemplateDto
import com.ab.wx.wx_lib.dto.WxUploadMaterialDto
import com.ab.wx.wx_lib.dto.qrcode.ExpiredQrCodeDto
import com.ab.wx.wx_lib.dto.qrcode.PermanentQrCodeDto
import com.ab.wx.wx_lib.dto.reply.MINIPROGRAMPAGE
import com.ab.wx.wx_lib.dto.reply.ReplyMiniAppDto
import com.ab.wx.wx_lib.fn.*
import com.ab.wx.wx_lib.vo.WxTicket
import com.ab.wx.wx_lib.vo.WxToken
import com.ab.wx.wx_lib.vo.WxUploadMaterialVo
import com.ab.wx.wx_lib.vo.wx.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.toEntity
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap

class Wx(wxConfigProperties: WxConfigProperties) {
    private val logger = LoggerFactory.getLogger(Wx::class.java)
    private val appId = wxConfigProperties.appId
    private val appSec = wxConfigProperties.appSec
    private val miniAppId = wxConfigProperties.miniAppId
    private val wxToken = wxConfigProperties.wxToken
//    private val restTemplate = getRestTemplate()

    init {
        WxConst.debug = wxConfigProperties.debug
    }

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

    /**
     * 生成二维码
     */
    private fun qrCodeUrl(token: String?) = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=${token}"

    /**
     * 展示二维码
     */
    private fun showQrCodeUrl(ticket: String) = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}"

    /**
     * 获取用户信息
     */
    private fun getUserInfoUrl(token: String?, openId: String) =
        "https://api.weixin.qq.com/cgi-bin/user/info?access_token=${token}&openid=${openId}&lang=zh_CN"


    /**
     * h5获取用户accesstoken
     */
    private fun getH5AccessTokenUrl(code: String) =
        "https://api.weixin.qq.com/sns/oauth2/access_token?appid=${appId}&secret=${appSec}&code=${code}&grant_type=authorization_code"

    /**
     * 获取h5用户
     */
    private fun getH5UserUrl(accessToken: String, openId: String) =
//        "https://api.weixin.qq.com/sns/auth?access_token=${accessToken}&openid=${openId}"
        "https://api.weixin.qq.com/sns/userinfo?access_token=${accessToken}&openid=${openId}&lang=zh_CN"

    /**
     * 发送客服消息
     */
    private fun sendCustomerMsg(accessToken: String) =
        "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=$accessToken"

    /**
     * 上传永久
     */
    private fun uploadMaterialUrl(accessToken: String) =
        "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=${accessToken}"


    /**
     * 分账
     */

    /**
     * 获取token
     */
    fun genToken(): String {
        val restTemplate = RestTemplate()
        val res = restTemplate.getForObject(getTokenUrl, WxToken::class.java)
        logger.info("getToken:$res")
        res?.let {
            WxConst.accessToken = res.access_token
            getTicket(res.access_token)
            return WxConst.accessToken
        }
        return ""
    }

    fun setToken(token: String) {
        WxConst.accessToken = token
    }

    fun getTicket(token: String?) {
//        restTemplate.getForObject(getTicketUrl(token), WxTicket::class.java)?.let {
//            logger.info("获取ticket:${it.ticket}")
//            WxConst.ticket = it.ticket
//        }
        restClient.get().uri(getTicketUrl(token)).accept(MediaType.APPLICATION_JSON).retrieve()
            .body(WxTicket::class.java)?.let {
                logger.info("获取 ticket:${it.ticket}")
                WxConst.ticket = it.ticket
            }
    }

    fun setTicket(ticket: String) {
        WxConst.ticket = ticket
    }


    fun createSign(jsapiTicket: String, url: String): HashMap<String, String> {
        var jsapi_ticket = jsapiTicket
        if (jsapi_ticket.isBlank()) {
            getTicket(WxConst.accessToken)
            jsapi_ticket = WxConst.ticket
        }
        logger.info("creteSign:$jsapi_ticket")
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
        return restClient.post().uri(createMenuUrl(WxConst.accessToken)).contentType(MediaType.APPLICATION_JSON)
            .contentLength(getContentLength(dto)).body(dto).retrieve().toEntity(WxCreateMenuVo::class.java).body
    }


    fun sendTemplate(dto: WxSendTemplateDto): WxTemplateVo? {
//        val entity = HttpEntity(dto, getHeaders())
//        logger.info("sendTemplate:$entity")
//        return restTemplate.postForObject(templateUrl(WxConst.accessToken), entity, WxTemplateVo::class.java)
        return restClient.post().uri(templateUrl(WxConst.accessToken)).contentType(MediaType.APPLICATION_JSON).body(dto)
            .contentLength(getContentLength(dto)).retrieve().toEntity<WxTemplateVo>().body

    }

    fun getSendTemplateUrl(): String {
        return templateUrl(WxConst.accessToken)
    }

    /**
     * 生成永久二维码
     */
    fun genPermanentQrCode(dto: PermanentQrCodeDto): WxQrCodeVo? {
        val entity = HttpEntity(dto, getHeaders())
        logger.info("qrCodeUrl:${qrCodeUrl(WxConst.accessToken)}")
//        return restTemplate.postForObject(qrCodeUrl(WxConst.accessToken), entity, WxQrCodeVo::class.java)
        return restClient.post().uri(qrCodeUrl(WxConst.accessToken)).contentLength(getContentLength(dto))
            .contentType(MediaType.APPLICATION_JSON).body(dto).retrieve().toEntity<WxQrCodeVo>().body
    }

    /**
     * 生成临时二维码
     */
    fun genExpiredQrCode(dto: ExpiredQrCodeDto): WxQrCodeVo? {
        val entity = HttpEntity<ExpiredQrCodeDto>(dto, getHeaders())
//        return restTemplate.postForObject(qrCodeUrl(WxConst.accessToken), entity, WxQrCodeVo::class.java)
        return restClient.post().uri(qrCodeUrl(WxConst.accessToken)).body(dto).contentType(MediaType.APPLICATION_JSON)
            .contentLength(getContentLength(dto)).retrieve().toEntity<WxQrCodeVo>().body
    }

    /**
     * 生成永久二维码的图形
     */
    fun showQrCode(dto: PermanentQrCodeDto): WxQrCodeUrlVo? {
        val qrCodeVo = genPermanentQrCode(dto)
        return qrCodeVo?.ticket?.let {
            WxQrCodeUrlVo(url = qrCodeVo.url, showUrl = showQrCodeUrl(it), ticket = it)
        }
    }

    /**
     * 获取用户信息
     */
    fun getUserInfo(openId: String): WxGetUserInfoVo? {
//        return restTemplate.getForObject(getUserInfoUrl(WxConst.accessToken, openId), WxGetUserInfoVo::class.java)
        val res =
            restClient.post().uri(getUserInfoUrl(WxConst.accessToken, openId)).contentType(MediaType.APPLICATION_JSON)
                .retrieve().toEntity<String>().body
        logger.info("获取用户信息:${res}")
        return if (res != null) getMapper().readValue(res, WxGetUserInfoVo::class.java)
        else null
    }

    /**
     * 获取appid
     */
    fun getAppId(): String {
        return appId
    }

    /**
     * 获取h5 用户 accesstoken
     */
    fun getH5UserAccessToken(code: String): WxH5AccessTokenVo? {
        val accessTokenUrl = getH5AccessTokenUrl(code)
        logger.info("accessTokenUrl:$accessTokenUrl")
//        return restTemplate.getForObject(accessTokenUrl, WxH5AccessTokenVo::class.java)
        return restClient.get().uri(accessTokenUrl).accept(MediaType.APPLICATION_JSON).retrieve()
            .toEntity<WxH5AccessTokenVo>().body
    }

    fun getH5User(code: String): WxUserVo? {
        var user: WxUserVo? = null
        val accessToken = getH5UserAccessToken(code)
        accessToken?.let {
            logger.info("getH5User:$it")
            if (it.access_token.isBlank()) throw RuntimeException("获取accessToken失败:${it.errmsg}")
            val token = it.access_token
            val openid = it.openid
//            restTemplate.getForObject(getH5UserUrl(token, openid), WxUserVo::class.java)?.let {
//                logger.info("getUser:$it")
//                user = it
//            }
            restClient.get().uri(getH5UserUrl(token, openid)).accept(MediaType.APPLICATION_JSON).retrieve()
                .toEntity<WxUserVo>().body?.let {
                    user = it
                }
        }
        return user
    }

    /**
     * 发送小程序消息
     */
    fun sendMiniAppMsg(touser: String, title: String, pagepath: String, thumb_media_id: String) {
        val dto = ReplyMiniAppDto(
            touser = touser, miniprogrampage = MINIPROGRAMPAGE(
                title = title, appid = miniAppId, pagepath = pagepath, thumb_media_id = thumb_media_id
            )
        )
        logger.info("发送客户消息:$dto")
//        val entity = HttpEntity(dto, getHeaders())
//        restTemplate.postForObject(sendCustomerMsg(WxConst.accessToken), entity, HashMap::class.java)?.let {
//            logger.info("发送客服消息回复:$it")
//        }
        restClient.post().uri(sendCustomerMsg(WxConst.accessToken)).contentType(MediaType.APPLICATION_JSON).body(dto)
            .contentLength(getContentLength(dto)).retrieve()
    }

    /**
     * 上传图片获取media_id
     */
    fun uploadMaterial(dto: WxUploadMaterialDto): WxUploadMaterialVo? {
        val headers: HttpHeaders = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        val params = LinkedMultiValueMap<String, Any>()
        params["media"] = FileSystemResource(dto.file)
        val entity = HttpEntity(params, headers)
//        val res = restTemplate.postForObject(uploadMaterialUrl(WxConst.accessToken), entity, String::class.java)
        val res = restClient.post().uri(uploadMaterialUrl(WxConst.accessToken)).contentType(MediaType.APPLICATION_JSON)
            .headers {
                it.addAll(headers)
            }.body(params).contentLength(getContentLength(params)).retrieve().toEntity<String>().body
        res?.let {
            if (res.contains("err")) {
                logger.error("上传素材失败:$res")
            } else {
                val vo = ObjectMapper().readValue(res, WxUploadMaterialVo::class.java)
                logger.info("上传素材成功:$vo")
                return vo
            }
        }
        return null
    }

}