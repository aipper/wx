package com.ab.wx.wx_lib.config

import com.ab.wx.wx_lib.fn.logger
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class ContentLengthInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        // 检查 Content-Length 是否已存在
        val contentLength = request.headers["Content-Length"]?.get(0)

        // 如果未设置并且 body 不是空，则自动设置 Content-Length
        if (contentLength == null && body.isNotEmpty()) {
            val length = body.size
            request.headers.contentLength = length.toLong()
        }

        // 日志记录
        logger("Content-Length set to: ${request.headers.contentLength}")

        return execution.execute(request, body)
    }
}