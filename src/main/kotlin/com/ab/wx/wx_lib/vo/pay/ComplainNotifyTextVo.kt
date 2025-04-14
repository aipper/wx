package com.ab.wx.wx_lib.vo.pay

import java.time.LocalDateTime
import java.time.OffsetDateTime

data class ComplainNotifyTextVo(
    val id: String = "",
    val create_time: OffsetDateTime? = null,
    val event_type: String = "",
    val resource_type: String = "",
    val summary: String = "",
    val resource: ComplaintNotifyTextResource? = null,

    )

data class ComplaintNotifyTextResource(
    val algorithm: String = "",
    val ciphertext: String = "",
    val original_type: String = "",
    val associated_data: String = "",
    val nonce: String = ""
)

data class DecodeResourceVo(
    val complaint_id: String = "", val action_type: ActionTypeEnums
)

enum class ActionTypeEnums(val desc: String) {
    CREATE_COMPLAINT("用户提交投诉"),
    CONTINUE_COMPLAINT("用户继续投诉"),
    USER_RESPONSE("用户新留言"),
    RESPONSE_BY_PLATFORM("平台新留言"),
    SELLER_REFUND("商户发起全额退款"),
    MERCHANT_RESPONSE("商户新回复"),
    MERCHANT_CONFIRM_COMPLETE("商户反馈处理完成"),
    USER_APPLY_PLATFORM_SERVICE("用户申请平台协助"),
    USER_CANCEL_PLATFORM_SERVICE("用户取消平台协助"),
    PLATFORM_SERVICE_FINISHED("客服结束平台协助")
}
