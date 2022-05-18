package com.ab.wx.wx_lib.fn

import com.ab.wx.wx_lib.const.BaseConst
import com.ab.wx.wx_lib.const.R
import java.security.MessageDigest
import java.util.*

fun ok(code: Int = BaseConst.success, msg: String = "", data: Any? = null): R {
    return R(code, msg, data)
}

fun fail(code: Int = BaseConst.fail, msg: String = "", data: Any? = null): R {
    return R(code, msg, data)
}

fun sha1(str: String): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val result = digest.digest(str.toByteArray())
    return toHex(result)
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
    hash.forEach {
        formatter.format("%02x", it)
    }
    val result = formatter.toString()
    formatter.close()
    return result
}


fun create_nonce_str(): String {
    return UUID.randomUUID().toString()
}

fun create_timestamp(): String {
    return "${System.currentTimeMillis() / 1000}"
}
