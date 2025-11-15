# wx_lib 迁移指南

本指南帮助已有使用 `Wx`/`WxPay` 的项目升级到新的 `payment` 模块。新的结构提供统一的 `PaymentClient` 接口与多商户/服务商支持，并内置微信与支付宝实现。

## 1. 配置示例

```yaml
wx:
  payment:
    default-merchant: defaultWx
    merchants:
      defaultWx:
        mode: DIRECT
        app-id: wx123
        mchid: 1900000001
        serial-no: 6A28...
        api-v3-key: xxxx
        private-key: |
          -----BEGIN PRIVATE KEY-----
          ...
          -----END PRIVATE KEY-----
        notify-url: https://demo.com/wx/notify
        refunds-notify-url: https://demo.com/wx/refunds
        cert-refresh-minutes: 10
      spWx:
        mode: SERVICE_PROVIDER
        app-id: wx_sp123
        mchid: 1900000999
        serial-no: 123ABC...
        api-v3-key: yyyy
        key-path: /etc/wx/sp.pem
        sub-merchants:
          shopA:
            mchid: 1900009911
            app-id: wx_shopA
            notify-url: https://demo.com/wx/shopA/notify

alipay:
  payment:
    default-merchant: aliMain
    merchants:
      aliMain:
        app-id: 202100000000001
        private-key-path: /etc/ali/app_private_key.pem
        alipay-public-key-path: /etc/ali/alipay_public_key.pem
        endpoint: https://openapi.alipay.com/gateway.do
        notify-url: https://demo.com/alipay/notify
        return-url: https://demo.com/alipay/return
```

## 2. 注入方式

```kotlin
@RestController
class PaymentController(
    @Qualifier("wechatPaymentClient")
    private val wechatClient: PaymentClient,
    @Qualifier("alipayPaymentClient")
    private val alipayClient: PaymentClient
) {
    @PostMapping("/pay/wx/jsapi")
    fun wxPay(@RequestBody request: WxOrderReq): CreateOrderResult {
val cmd = CreateOrderCommand(
    merchantId = request.merchantId,
    subMerchantId = request.subMerchantId,
    channel = PaymentChannel.WECHAT_MINI_APP,
        outTradeNo = request.outTradeNo,
        description = request.description,
        amount = Money(request.amountFen),
        payer = Payer(openId = request.openId),
        notifyUrl = request.notifyUrl
        )
        return wechatClient.createOrder(cmd)
    }
}

// 支付宝小程序（需传入 userId，即支付宝 user_id）
val aliCmd = CreateOrderCommand(
    merchantId = request.merchantId,
    channel = PaymentChannel.ALIPAY_MINI_PROGRAM,
    outTradeNo = request.outTradeNo,
    description = request.title,
    amount = Money(request.amountFen),
    payer = Payer(userId = request.alipayUserId)
)
val aliResult = alipayClient.createOrder(aliCmd) // 返回 tradeNo
```

## 3. 回调验签

微信：

```kotlin
@PostMapping("/wx/notify")
fun handleWxNotify(
    @RequestHeader headers: HttpHeaders,
    @RequestBody body: String
): ResponseEntity<String> {
    val result = (wechatClient as WechatPayClient)
        .parsePaymentNotification(merchantId = null, subMerchantId = null, headers = headers, body = body)
    return if (result != null) ResponseEntity.ok("{\"code\":\"SUCCESS\"}") else ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"code\":\"FAIL\"}")
}
```

支付宝：

```kotlin
@PostMapping("/alipay/notify")
fun handleAliNotify(@RequestParam params: Map<String, String>): String {
    val notification = (alipayClient as AlipayClient).parsePaymentNotification(null, params)
    return if (notification != null && notification.status == PaymentStatus.SUCCESS) "success" else "fail"
}
```

## 4. 常见问题

1. **平台证书刷新频率**：默认 10 分钟，按需配置 `cert-refresh-minutes`。失败会在下一次通知或 API 调用时自动重试。
2. **Maven 构建**：首次构建需要外网访问 Maven 仓库。若网络受限，可将依赖预下载或配置公司内部仓库。
3. **Bean 名称冲突**：新增 Bean 名称 `wechatPaymentClient`、`alipayPaymentClient`。旧的 `WxAutoConfig`/`WxPayAutoConfig` 已弃用，迁移后请直接注入新的客户端。
