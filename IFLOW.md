# wx_lib 微信支付库项目文档

## 项目概述

wx_lib 是一个基于 Kotlin 和 Spring Boot 3 的微信支付/公众号/小程序集成库，版本 2.1.12。该项目提供了完整的微信生态接入能力，包括支付、公众号API、小程序API等功能，采用 Spring Boot 自动配置机制，便于其他业务项目集成使用。

### 技术栈
- **语言**: Kotlin 2.0.0
- **框架**: Spring Boot 3.3.2
- **构建工具**: Maven
- **JDK版本**: Java 17
- **主要依赖**: Spring Boot Web、Jackson XML、Spring Boot Configuration Processor

## 项目结构

```
src/main/kotlin/com/ab/wx/wx_lib/
├── config/          # 自动配置和属性绑定
├── const/           # 常量定义
├── dto/             # 数据传输对象
│   ├── miniapp/     # 小程序相关DTO
│   ├── pay/         # 支付相关DTO
│   ├── qrcode/      # 二维码相关DTO
│   └── reply/       # 回复消息DTO
├── enums/           # 枚举类
├── exception/       # 异常处理
├── fn/              # 公共函数和工具类
│   └── aes/         # AES加密相关
├── services/        # 业务服务层
├── task/            # 任务相关
├── vo/              # 值对象
│   ├── miniapp/     # 小程序相关VO
│   ├── pay/         # 支付相关VO
│   └── wx/          # 微信相关VO
└── wx/              # 微信核心功能类
    ├── MiniApp.kt   # 小程序功能
    ├── Wx.kt        # 公众号功能
    └── WxPay.kt     # 支付功能
```

## 构建和运行

### 构建命令
```bash
# 编译项目（跳过测试）
./mvnw clean package -DskipTests

# 运行测试
./mvnw test

# 本地安装到Maven仓库
./mvnw install

# 发布到远程仓库（需要配置distributionManagement）
./mvnw deploy
```

**注意**: Windows用户请使用 `mvnw.cmd` 替代 `./mvnw`。

### 项目配置
项目通过Spring Boot的自动配置机制提供集成能力，主要配置类为：
- `WxAutoConfig`: 主自动配置类
- `WxConfigProperties`: 配置属性绑定类

配置前缀为 `wx`，支持以下主要配置项：
- `wx.appId`: 微信公众号AppID
- `wx.appSec`: 微信公众号AppSecret
- `wx.miniAppId`: 小程序AppID
- `wx.miniAppSec`: 小程序AppSecret
- `wx.wxToken`: 微信Token
- `wx.pay.*`: 支付相关配置

## 开发约定

### 代码风格
- 遵循Kotlin官方代码风格
- 使用4空格缩进，UTF-8编码
- 包名全小写点分，类/对象使用PascalCase
- 方法/属性使用camelCase，常量使用UPPER_SNAKE_CASE
- 公共API使用KDoc文档

### 架构原则
- 保持包内职责单一，避免循环依赖
- 公共类型放置于`dto/vo/enums/const/config/services`等目录
- 专注于提供可复用的单元，不包含业务编排

### 测试规范
- 使用JUnit 5测试框架
- 测试文件位于`src/test/kotlin`，以`*Test.kt`结尾
- 优先覆盖加解密(`fn/aes`)、业务服务(`services`)与配置装配(`config`)

## 核心功能模块

### 1. 微信支付 (WxPay.kt)
提供完整的微信支付V3 API集成，包括：
- JSAPI支付、H5支付、小程序支付
- 退款功能
- 商户转账
- 分账功能（添加/删除接收方、请求分账、分账退回）
- 投诉处理
- 证书管理和自动更新

### 2. 公众号API (Wx.kt)
提供公众号相关功能：
- Access Token和Ticket管理
- 菜单创建
- 模板消息发送
- 二维码生成（永久/临时）
- 用户信息获取
- 素材上传

### 3. 小程序API (MiniApp.kt)
提供小程序相关功能：
- Access Token管理
- 登录凭证校验
- 手机号获取
- 统一服务消息

## 安全特性

### 加密和签名
- 支持RSA签名验证（SHA256withRSA）
- AES加密敏感信息
- 防重放攻击机制
- 证书自动管理和更新

### 支付安全
- 支付回调验签
- 敏感信息加密传输
- 分账资金安全控制

## 使用示例

### 基本配置
```yaml
wx:
  app-id: your_app_id
  app-sec: your_app_secret
  mini-app-id: your_mini_app_id
  mini-app-sec: your_mini_app_secret
  wx-token: your_wx_token
  pay:
    mchid: your_mch_id
    notify-url: your_notify_url
    key-path: path/to/your/private_key
    serial-no: your_serial_no
    v3key: your_v3_key
```

### 发起支付
```kotlin
@Autowired
private lateinit var wxPay: WxPay

val payDto = SimplePayDto(
    description = "商品描述",
    orderNo = "order123",
    amount = 100, // 金额，单位：分
    payOpenid = "user_openid",
    profitSharing = false // 是否分账
)

val result = wxPay.genSimplePay(payDto, "POST")
```

### 创建菜单
```kotlin
@Autowired
private lateinit var wx: Wx

val menuDto = WxCreateMenuDto(/* 菜单配置 */)
val result = wx.createMenu(menuDto)
```

## 部署和维护

### 依赖管理
项目通过Maven管理依赖，主要依赖包括：
- Spring Boot Starter Web
- Spring Boot Starter Log4j2
- Jackson Dataformat XML
- Kotlin Reflect和Stdlib

### 版本兼容性
- Java 17+
- Spring Boot 3.3.x
- Kotlin 2.x

## 贡献指南

### 提交规范
推荐使用Conventional Commits格式：
- `feat: 新增功能`
- `fix: 修复问题`
- `test: 测试相关`
- `docs: 文档更新`

### PR要求
- 说明变更动机与范围
- 关联相关Issue
- 包含必要测试
- 保持最小化变更

## 注意事项

1. **库项目定位**: 本项目为封装库，供其他业务项目集成使用，不内置业务编排
2. **回调处理**: 回调类能力仅提供验签与解密，具体调用时机由集成方决定
3. **证书管理**: 支持本地证书和按需拉取，不包含定时轮转调度
4. **分账限制**: 仅支持对商户类型的分账单执行回退操作
5. **测试覆盖**: 重点关注加解密、业务服务和配置装配的测试覆盖

## 联系和支持

如有问题或建议，请通过项目的Issue系统进行反馈。