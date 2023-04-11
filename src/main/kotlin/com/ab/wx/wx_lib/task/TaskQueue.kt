package com.ab.wx.wx_lib.task

import java.util.concurrent.DelayQueue

object TaskQueue {
    private val delayQueue = DelayQueue<Task>()

    fun put(task: Task) {
        removeTask(task.token)
        delayQueue.put(task)
    }

    private fun removeTask(token: String?) {
        delayQueue.removeIf { item -> item.token == token }
    }

    fun take(): Task {
        return delayQueue.take()
    }

}
