package com.ab.wx.wx_lib.const

import com.ab.wx.wx_lib.exception.WxPayException

/**
 * 响应结果封装
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val exception: WxPayException) : Result<Nothing>()

    fun isSuccess(): Boolean = this is Success

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw exception
    }
}