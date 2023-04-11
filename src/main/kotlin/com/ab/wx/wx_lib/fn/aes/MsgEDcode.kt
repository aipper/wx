package com.ab.wx.wx_lib.fn.aes

import com.ab.wx.wx_lib.fn.getBase64Decoder
import com.ab.wx.wx_lib.fn.getBase64Encoder
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


// 生成4个字节的网络字节序
fun getNetworkBytesOrder(sourceNumber: Int): ByteArray {
    val orderBytes = ByteArray(4)
    orderBytes[3] = (sourceNumber and 0xFF).toByte()
    orderBytes[2] = (sourceNumber shr 8 and 0xFF).toByte()
    orderBytes[1] = (sourceNumber shr 16 and 0xFF).toByte()
    orderBytes[0] = (sourceNumber shr 24 and 0xFF).toByte()
    return orderBytes
}

// 还原4个字节的网络字节序
fun recoverNetworkBytesOrder(orderBytes: ByteArray): Int {
    var sourceNumber = 0
    for (i in 0..3) {
        sourceNumber = sourceNumber shl 8
        sourceNumber = sourceNumber or (orderBytes[i].toInt() and 0xff)
    }
    return sourceNumber
}

// 随机生成16位字符串
fun getRandomStr(): String {
    val base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val random = Random()
    val sb = StringBuffer()
    for (i in 0..15) {
        val number: Int = random.nextInt(base.length)
        sb.append(base[number])
    }
    return sb.toString()
}

/**
 * 对明文进行加密.
 *
 * @param text 需要加密的明文
 * @return 加密后base64编码的字符串
 */
fun encrypt(randomStr: String, text: String, appId: String, aesKey: ByteArray): String? {
    val byteCollector = ByteGroup()
    val randomStrBytes: ByteArray = randomStr.toByteArray(CHARSET)
    val textBytes: ByteArray = text.toByteArray(CHARSET)
    val networkBytesOrder = getNetworkBytesOrder(textBytes.size)
    val appidBytes: ByteArray = appId.toByteArray(CHARSET)

    // randomStr + networkBytesOrder + text + appid
    byteCollector.addBytes(randomStrBytes)
    byteCollector.addBytes(networkBytesOrder)
    byteCollector.addBytes(textBytes)
    byteCollector.addBytes(appidBytes)

    // ... + pad: 使用自定义的填充方式对明文进行补位填充
    val padBytes: ByteArray = encode(byteCollector.size())
    byteCollector.addBytes(padBytes)

    // 获得最终的字节流, 未加密
    val unencrypted: ByteArray = byteCollector.toBytes()

    // 设置加密模式为AES的CBC模式
    val cipher: Cipher = Cipher.getInstance("AES/CBC/NoPadding")
    val keySpec = SecretKeySpec(aesKey, "AES")
    val iv = IvParameterSpec(aesKey, 0, 16)
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)

    // 加密
    val encrypted: ByteArray = cipher.doFinal(unencrypted)

    // 使用BASE64对加密后的字符串进行编码
    return getBase64Encoder().encodeToString(encrypted)
}

/**
 * 对密文进行解密.
 *
 * @param text 需要解密的密文
 * @return 解密得到的明文
 * @throws AesException aes解密失败
 */
fun decrypt(text: String?, aesKey: ByteArray): String {
    val original: ByteArray
    // 设置解密模式为AES的CBC模式
    val cipher = Cipher.getInstance("AES/CBC/NoPadding")
    val key_spec = SecretKeySpec(aesKey, "AES")
    val iv = IvParameterSpec(aesKey.copyOfRange(0, 16))
    cipher.init(Cipher.DECRYPT_MODE, key_spec, iv)

    // 使用BASE64对密文进行解码
    val encrypted: ByteArray = getBase64Decoder().decode(text)

    // 解密
    original = cipher.doFinal(encrypted)

    val xmlContent: String
    val from_appid: String
    val bytes: ByteArray = decode(original)

    // 分离16位随机字符串,网络字节序和AppId
    val networkOrder = Arrays.copyOfRange(bytes, 16, 20)
    val xmlLength = recoverNetworkBytesOrder(networkOrder)
    xmlContent = String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET)
    from_appid = String(
        bytes.copyOfRange(20 + xmlLength, bytes.size), CHARSET
    )
    return xmlContent
}
