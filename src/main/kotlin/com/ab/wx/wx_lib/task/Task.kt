package com.ab.wx.wx_lib.task

import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

data class Task(val time: Long, val token: String?) : Delayed {
    private val expiredTime by lazy { System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(time) }
    override fun compareTo(other: Delayed?): Int {
        val res = expiredTime - (other as Task).expiredTime
        return if (res <= 0) {
            -1
        } else {
            1
        }
    }

    override fun getDelay(unit: TimeUnit): Long {
        return expiredTime - System.currentTimeMillis()
    }
}

