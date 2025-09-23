package com.ab.wx.wx_lib.vo.pay


data class ComplaintOrderInfo(
    val transaction_id: String = "",
    val out_trade_no: String = "",
    val amount: Int = 0,
)

data class ServerOrder(
    val order_id: String = "", val out_order_no: String = "", val state: ServerOrderState = ServerOrderState.DOING
)

data class ComplaintMedia(
    val media_type: String = "", val media_url: String = ""
)

data class SearchComplaintList(
    /**
     * 【投诉单号】 投诉单对应的投诉单号
     */
    val complaint_id: String = "",/*
    【投诉时间】 投诉时间
     */
    val complaint_time: String = "",
    val complaint_detail: String = "",
    val complaint_state: ComplaintState = ComplaintState.PENDING,
    val payer_phone: String = "",
    val complaint_order_info: List<ComplaintOrderInfo> = arrayListOf(),
    val complaint_full_refunded: Boolean = false,
    val incoming_user_response: Boolean = false,
    val user_complaint_times: Int = 0,
    val complaint_media_list: List<ComplaintMedia> = arrayListOf(),
    val problem_description: String = "",
    val problem_type: ProblemType = ProblemType.REFUND,

    val apply_refund_amount: Int = 0,
    val user_tag_list: List<UserTag> = arrayListOf(),
    val service_order_info: List<ServerOrder> = arrayListOf(),
    val additional_info: Any? = null,
    val in_platform_service: Boolean = false,
    val need_immediate_service: Boolean = false
)

data class SearchComplaintPagesVo(
    val data: List<SearchComplaintList> = arrayListOf(),
    val total_count: Int = 0,
    val offset: Int = 0,
    val limit: Int = 0
)

enum class ComplaintState {
    PENDING, PROCESSING, PROCESSED
}

enum class ProblemType {
    REFUND, SERVICE_NOT_WORK, OTHERS
}

enum class UserTag {
    TRUSTED, HIGH_RISK
}

enum class ServerOrderState {
    DOING, REVOKED, WAITPAY, DONE
}