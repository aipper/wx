package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.config.WxMappingJackson2HttpMessageConverter
import com.ab.wx.wx_lib.const.BaseConst
import com.ab.wx.wx_lib.const.R
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.util.*

fun ok(data: Any? = null, code: Int = BaseConst.success, msg: String = ""): R {
    return R(code, msg, data)
}

fun fail(code: Int = BaseConst.fail, msg: String = "", data: Any? = null): R {
    return R(code, msg, data)
}

fun sha1(str: String): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val result = digest.digest(str.toByteArray())
    return byteToHex(result)
}

fun toHex(byteArray: ByteArray): String {
    //转成16进制
    val result = with(StringBuilder()) {
        byteArray.forEach {
            val value = it
            val hex = value.toInt() and (0xFF)
            val hexStr = Integer.toHexString(hex)
            if (hexStr.length == 1) {
                append("0").append(hexStr)
            } else {
                append(hexStr)
            }
        }
        this.toString()
    }
    return result
}

fun byteToHex(hash: ByteArray): String {
    val formatter = Formatter()
    var result = ""
    formatter.use {
        hash.forEach {
            formatter.format("%02x", it)
        }
        result = formatter.toString()

    }
    return result
}


fun create_nonce_str(): String {
    return UUID.randomUUID().toString()
}

fun create_timestamp(): String {
    return "${System.currentTimeMillis() / 1000}"
}

fun getRestTemplate(): RestTemplate {
    val res = RestTemplate()
    res.messageConverters.add(WxMappingJackson2HttpMessageConverter())
    return res
}


fun getBase64Encoder(): Base64.Encoder {
    return Base64.getEncoder()
}

fun getBase64Decoder(): Base64.Decoder {
    return Base64.getDecoder()
}
fun getHeaders(): HttpHeaders {
    val header = HttpHeaders()
    header.contentType = MediaType.APPLICATION_JSON
    return header
}