package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.config.WxMappingJackson2HttpMessageConverter
import com.ab.wx.wx_lib.const.BaseConst
import com.ab.wx.wx_lib.const.R
import com.ab.wx.wx_lib.exception.RestErrHandler
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.util.*
import java.util.stream.Collectors


private val logger = LoggerFactory.getLogger("Fn")
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

fun create_pay_nonce(): String {
    return create_nonce_str().replace("-", "")
}

fun create_timestamp(): String {
    return "${System.currentTimeMillis() / 1000}"
}

fun getRestTemplate(): RestTemplate {
    val factory = SimpleClientHttpRequestFactory()
    factory.setOutputStreaming(false)
    val res = RestTemplate(factory)
    val stringHttpMessageConverter = StringHttpMessageConverter()
    stringHttpMessageConverter.supportedMediaTypes =
        arrayListOf(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.TEXT_HTML)
    res.messageConverters.add(WxMappingJackson2HttpMessageConverter())
    res.messageConverters.add(stringHttpMessageConverter)
    res.errorHandler = RestErrHandler()
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
    header.accept = arrayListOf(MediaType.APPLICATION_JSON)
//    header.accept.add(MediaType.APPLICATION_JSON)
    return header
}

fun getPayHeaders(token: String): HttpHeaders {
    logger.info("token:$token")
    val header = HttpHeaders()
    header.contentType = MediaType.APPLICATION_JSON
    header.accept = arrayListOf(MediaType.APPLICATION_JSON)
//    header.accept.add(MediaType.APPLICATION_JSON)
//    header.set("Authorization",token)
    header.add("Authorization", token)
    header.add("User-Agent", "Mozilla/5.0")
    return header
}



fun getMapper(): ObjectMapper {
    val mapper = ObjectMapper()
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    return mapper
}

fun readIns(input: InputStream): String {
    InputStreamReader(input).use {
        BufferedReader(it).use {
            val stringBuilder = StringBuilder()
            var line: String? = it.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = it.readLine()
            }
            return stringBuilder.toString()
        }
    }
}

fun genPaySign(vararg components: String): String {
    return Arrays.stream(components).collect(Collectors.joining("\n", "", "\n"))
}

fun genX509Cert() {
}