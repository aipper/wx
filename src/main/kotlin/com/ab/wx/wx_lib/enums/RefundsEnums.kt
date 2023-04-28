package com.ab.wx.wx_lib.enums

enum class RefundsEnums (val code:String,val desc:String){
    SUCCESS("SUCCESS","退款成功"),
    CLOSED("CLOSED","退款关闭"),
    PROCESSING("PROCESSING","退款处理中"),
    ABNORMAL("ABNORMAL","退款异常")
}