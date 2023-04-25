package com.ab.wx.wx_lib.vo.pay

import java.time.ZonedDateTime

data class PayCertResVo(
    val data: List<PayCert> = arrayListOf()
)

data class PayCert(
    val serial_no: String = "",
    val effective_time: ZonedDateTime = ZonedDateTime.now(),
    val expire_time: ZonedDateTime = ZonedDateTime.now(),
    val encrypt_certificate: PayCertEncryptCert = PayCertEncryptCert()
)

data class PayCertEncryptCert(
    val algorithm: String = "", val nonce: String = "", val associated_data: String = "", val ciphertext: String = ""
)
