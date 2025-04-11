package com.ab.wx.wx_lib.services

import com.ab.wx.wx_lib.dto.pay.TransPayDto
import com.ab.wx.wx_lib.vo.pay.TransferCallbackVo
import com.ab.wx.wx_lib.vo.pay.TransferVo
import jakarta.servlet.http.HttpServletRequest

interface WxTransferService {
    /**
     * 执行转账操作
     * @param dto 转账请求数据
     * @return 转账结果
     */
    fun transfer(dto: TransPayDto): Result<TransferVo>

    /**
     * 处理转账回调
     * @param request HTTP请求
     * @return 转账回调结果
     */
    fun handleTransferCallback(request: HttpServletRequest): Result<TransferCallbackVo>
}