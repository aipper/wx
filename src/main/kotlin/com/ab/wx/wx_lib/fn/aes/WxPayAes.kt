package com.ab.wx.wx_lib.fn.aes

import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object WxPayAes {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private const val KEY_LENGTH_BYTE = 32
    private const val TAG_LENGTH_BIT = 128


    @Throws(GeneralSecurityException::class)
    fun decryptToString(
        associatedData: ByteArray?, nonce: ByteArray?, ciphertext: String?, apiV3Key: ByteArray
    ): String? {
        return try {
            val key = SecretKeySpec(apiV3Key, "AES")
            val spec = GCMParameterSpec(TAG_LENGTH_BIT, nonce)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            cipher.updateAAD(associatedData)
            String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), StandardCharsets.UTF_8)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException(e)
        } catch (e: NoSuchPaddingException) {
            throw IllegalStateException(e)
        } catch (e: InvalidKeyException) {
            throw IllegalArgumentException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw IllegalArgumentException(e)
        }
    }
}