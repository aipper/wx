package com.ab.wx.wx_lib.dto

import com.ab.wx.wx_lib.enums.MenuTypeEnums

data class WxMenuDto(
    /**
     * 菜单名称
     */
    val name: String = "",
    /**
     * 子级菜单
     */
    val sub_button: List<WxMenuDto> = arrayListOf(),
    /**
     * 菜单类型
     */
    val type: String = MenuTypeEnums.CLICK.code,
    /**
     *  菜单类型为click是必须
     */
    val key: String? = null,
    /**
     * 菜单类型为view 小程序时必须
     */
    val url: String? = null,
    /**
     * media_id类型和view_limited类型必须
     */
    val media_id: String? = null,
    /**
     * 	miniprogram类型必须
     */
    val appid: String? = null,

    /**
     *	miniprogram类型必须
     */
    val pagepath: String? = null,
    /**
     * article_id类型和article_view_limited类型必须
     */
    val article_id: String? = null


)
