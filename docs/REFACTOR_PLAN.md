# wx_lib 重构计划

本计划聚焦于将 wx_lib 从单租户、仅支撑微信直连商户的工具库升级为多商户、服务商模式友好、同时抽象出可扩展支付接口（支持微信支付与支付宝）。方案参考了微信支付服务商接口文档（如《JSAPI 支付-服务商模式》、证书拉取 `/v3/certificates` 等）以及支付宝开放平台的下单/回调签名规范。

## 目标
1. **多商户与服务商模式**：能够为不同商户维护独立配置，支持服务商（sp_mchid/sub_mchid）与直连并存。
2. **API 覆盖**：微信支付至少覆盖 JSAPI、H5、Native、App 下单及退款/查询/关单；支付宝覆盖电脑网站、App、小程序等场景。
3. **统一的对外接口**：通过 `payment` 模块暴露通用的 Request/Result/Client 抽象，让业务代码屏蔽渠道差异。
4. **Starter 优化**：Spring Boot 自动装配可以按需注册多实例客户端、具备良好的属性绑定与条件装配。

## 分阶段交付

### Phase 1：现状梳理与基础设施
- 整理当前 `WxConfigProperties`、`WxPay`、DTO/VO 的职责，确认与官方文档差异。
- 在 `docs/` 中记录重构计划，列出兼容性注意事项。
- 引入 `payment` 核心抽象（客户端接口、命令/结果、渠道枚举），为多支付渠道提供统一入口。

### Phase 2：微信支付多商户化
- 新增 `WxPaymentProperties`（含 `defaultMerchant`、`merchants` Map、服务商配置）。
- 基于配置创建 `WechatPayClient`，内部维护多商户上下文、证书缓存与签名组件。
- 将现有 `WxPay` 能力迁移到 `WechatPayClient` 内部，补齐 JSAPI/H5/Native/App 下单、订单查询、关单、退款、证书刷新等 API。

### Phase 3：自动装配与上下文管理
- 提供 `WxPayAutoConfiguration` 新实现：按商户 ID 生成 `WechatPayClient` Bean，或暴露 `PaymentClientRegistry`。
- 提供服务商模式所需的 `SubMerchantContext`、`Notify` 解析工具，保证回调验签逻辑可注入。

### Phase 4：支付宝支持
- 依据支付宝开放平台网关协议，引入 `AlipayProperties` 与 `AlipayClient`，与 `PaymentClient` 接口对齐。
- 实现网页支付、APP 支付、退款、回调验签等基础 API，并复用统一抽象。

### Phase 5：对外 API 优化与迁移指引
- 输出 `MIGRATION.md`，指导旧项目从 `Wx`/`WxPay` 迁移到新的 `PaymentClient` 层。
- 更新 `spring.factories`、README，列出必要配置示例与常见问题。

## 当前进展
- 制定总体计划并引入 `payment` 核心抽象（进行中）。
- 设计 `WxPaymentProperties` 以支撑多商户配置（进行中）。

## 后续风险 & 注意事项
- 兼容历史依赖：保留旧类（`WxPay` 等）一段时间，但标注 `@Deprecated` 并给出替代方案。
- 证书与密钥管理：多商户下证书拉取频次需控制，建议集中缓存并定期刷新。
- 性能：多客户端实例需要注意 RestTemplate/HttpClient 复用。
