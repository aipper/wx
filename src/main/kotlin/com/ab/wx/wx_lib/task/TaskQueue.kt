package com.ab.wx.wx_lib.task

import java.util.concurrent.DelayQueue

object TaskQueue {
    private val delayQueue = DelayQueue<Task>()

    fun put(task: Task) {
        delayQueue.put(task)
    }

    fun take(): Task {
        return delayQueue.take()
    }

}
