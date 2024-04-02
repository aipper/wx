package com.ab.wx.wx_lib.dto.pay

data class TransPayDto(
    val appid: String,
    val out_batch_no: String,
    val batch_name: String,
    val batch_remark: String,
    val total_amount: Int,
    val total_num: Int,
    val transfer_detail_list: List<TransferDetail>,
    val transfer_scene_id: String = "",
    val notify_url: String? = ""
)

data class TransferDetail(
    val out_detail_no: String,
    val transfer_amount: Int,
    val transfer_remark: String,
    val openid: String,
    val user_name: String? = ""
)