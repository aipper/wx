package com.ab.wx.wx_lib.exception

import org.slf4j.LoggerFactory
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.DefaultResponseErrorHandler

class RestErrHandler : DefaultResponseErrorHandler() {
    private val logger = LoggerFactory.getLogger(RestErrHandler::class.java)
    override fun hasError(response: ClientHttpResponse): Boolean {
        logger.error("response:${response.statusText}")
        return false
    }
}