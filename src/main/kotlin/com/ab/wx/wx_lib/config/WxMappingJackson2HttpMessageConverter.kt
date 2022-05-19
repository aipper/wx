package com.ab.wx.wx_lib.config

import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter


class WxMappingJackson2HttpMessageConverter : MappingJackson2HttpMessageConverter() {
    init {
        val mediaTypes: MutableList<MediaType> = ArrayList()
        mediaTypes.add(MediaType.TEXT_PLAIN)
        mediaTypes.add(MediaType.TEXT_HTML)
        supportedMediaTypes = mediaTypes
    }
}