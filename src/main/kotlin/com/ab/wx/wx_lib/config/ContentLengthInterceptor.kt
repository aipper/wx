package com.ab.wx.wx_lib.config

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class ContentLengthInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val contentLength = request.headers["Content-Length"]?.get(0)
        if (contentLength == null) {
            val length = body.size
            request.headers.contentLength = length.toLong()
        }
        return execution.execute(request, body)
    }
}