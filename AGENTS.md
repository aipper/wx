# Repository Guidelines

> 本文档为贡献者快速上手指南，面向本仓库（Kotlin + Spring Boot 3，Maven 构建）的实际约定与操作方式。

## 项目结构与模块
- 源码：`src/main/kotlin`（包：`com.ab.wx.wx_lib.*`）
- 资源：`src/main/resources`（含 `META-INF/spring.factories` 与配置元数据）
- 测试（如新增）：`src/test/kotlin`
- 构建文件：`pom.xml`（Java 17，Kotlin 2.x，Spring Boot 3.3.x）

## 构建、测试与开发
- 构建产物：`./mvnw clean package -DskipTests`
- 运行测试：`./mvnw test`
- 本地安装：`./mvnw install`
- 发布（按需配置 `distributionManagement`）：`./mvnw deploy`
提示：Windows 使用 `mvnw.cmd`。库项目不提供可执行运行目标。

## 代码风格与命名
- 风格：遵循 Kotlin 官方代码风格；缩进 4 空格；UTF-8。
- 命名：包名全小写点分；类/对象 `PascalCase`；方法/属性 `camelCase`；常量使用 `UPPER_SNAKE_CASE`。
- 文档：公共 API 使用 KDoc 简述用途、参数与返回值。
- 约束：保持包内职责单一；避免循环依赖；公共类型放置于 `dto/vo/enums/const/config/services` 等既有目录。

## 测试规范
- 框架：JUnit 5（由 `spring-boot-starter-test` 提供）。
- 位置与命名：`src/test/kotlin`，文件以 `*Test.kt` 结尾，类名以 `Test` 结尾。
- 关注点：优先覆盖加解密（`fn/aes`）、业务服务（`services`）与配置装配（`config`）。
- 运行：`./mvnw test`；可用 `-Dtest=ClassNameTest` 定位单测。

## 提交与合并请求
- 提交信息：推荐 Conventional Commits，如 `feat: 新增支付签名工具`、`fix: 修复退款回调解析`、`test: 补充分账用例`。
- PR 要求：
  - 说明变更动机与范围，列出关键文件与影响面；
  - 关联 Issue（如有）；
  - 包含必要的测试或验证步骤；
  - 保持最小化变更、通过构建与测试。

## 架构速览（参考）
- `config/*` 自动装配与属性绑定；`services/*` 业务封装；`fn/*` 公共函数与加解密；`dto/vo/enums/const/*` 传输与常量；`wx/*` 微信相关入口封装。

## 使用定位与职责边界
- 本项目为“封装库”，供其他业务项目集成使用；不内置业务编排与自动化流程。
- 专注提供可复用的单元（签名、验签、加/解密、请求封装、投诉相关API封装等），保证接口正确性与稳定性。
- 回调类能力（如支付/退款/投诉通知）仅提供验签与解密及对应API调用单元；是否、何时调用由集成方的业务逻辑决定。
- 证书管理支持本地证书与按需拉取并择优选择；不包含定时轮转调度，若需自动化请由集成方按自身需求调度刷新。
