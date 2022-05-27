package com.ab.wx.wx_lib.scheduler

import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableAsync
@EnableScheduling
class Cron {


    @Scheduled(fixedRate = 2 * 3600 * 1000)
    fun task() {

    }
}