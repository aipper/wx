package com.ab.wx.wx_lib.dto.pay

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SearchComplaintListDto(
    val limit: Int = 50,
    val offset: Int = 0,
    @JsonIgnore
    val beginDate: LocalDate = LocalDate.now(),
    @JsonIgnore
    val endDate: LocalDate = LocalDate.now(),
    val complainted_mchid: String? = null
) {
    init {
        require(beginDate <= endDate) { "开始日期不能晚于结束日期" }
        require(limit > 0) { "limit 必须大于0" }
        require(offset >= 0) { "offset 不能为负数" }
    }

    val begin_date: String by lazy {
        DATE_FORMATTER.format(beginDate)
    }

    val end_date: String by lazy {
        DATE_FORMATTER.format(endDate)
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}

/**
 * LocalDate 扩展函数，格式化为 yyyy-MM-dd 字符串
 */
fun LocalDate.toFormattedDateString(): String = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(this)