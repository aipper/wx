package com.ab.wx.wx_lib.services

import com.ab.wx.wx_lib.dto.pay.AddReceiverDto
import com.ab.wx.wx_lib.dto.pay.DelReceiverDto
import com.ab.wx.wx_lib.dto.pay.RequestOrderDto
import com.ab.wx.wx_lib.dto.pay.UnfreezeDto
import com.ab.wx.wx_lib.vo.pay.AddReceiverVo
import com.ab.wx.wx_lib.vo.pay.DelReceiverVo
import com.ab.wx.wx_lib.vo.pay.RequestOrderVo
import com.ab.wx.wx_lib.vo.pay.UnfreezeVo


/**
 * 微信分账接口抽象
 */
interface WxProfitSharingService {
    /**
     * 添加分账接收方
     * @param dto 添加接收方请求数据
     * @return 添加结果
     */
    fun addReceiver(dto: AddReceiverDto): Result<AddReceiverVo>

    /**
     * 删除分账接收方
     * @param dto 删除接收方请求数据
     * @return 删除结果
     */
    fun deleteReceiver(dto: DelReceiverDto): Result<DelReceiverVo>

    /**
     * 请求分账
     * @param dto 分账请求数据
     * @return 分账结果
     */
    fun requestProfitSharing(dto: RequestOrderDto): Result<RequestOrderVo>

    /**
     * 解冻分账资金
     * @param dto 解冻资金请求数据
     * @return 解冻结果
     */
    fun unfreeze(dto: UnfreezeDto): Result<UnfreezeVo>
}
