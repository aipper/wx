# Repository Guidelines

## Project Structure & Module Organization
Kotlin sources live under `src/main/kotlin/com/ab/wx/wx_lib`, with subpackages that match the library’s responsibilities: `config` wires Spring Boot auto-configuration and property objects, `dto` and `vo` capture WeChat payload contracts, `fn` and `wx` contain the core encryption, scheduling, and API clients, and `task`/`scheduler` coordinate background work. Shared resources such as `META-INF/spring.factories` sit in `src/main/resources` to register the auto-config starter. Add new modules beside the existing package to keep the public surface predictable, and prefer colocating tests in the mirrored `src/test/kotlin` tree.

## Build, Test, and Development Commands
- `mvn clean install` – full build that compiles Kotlin sources, runs annotation processing, executes the test suite, and produces the published artifact.
- `mvn test` – fast feedback cycle for unit and integration tests only.
- `mvn -DskipTests package` – generate a jar quickly when you have already validated changes locally.
Run commands from the repository root; Maven already points to the Kotlin source directories configured in `pom.xml`.

## Coding Style & Naming Conventions
Use Kotlin formatting with four-space indentation, trailing commas disabled, and explicit visibility for public APIs. Package names remain under `com.ab.wx.wx_lib.*`, while files that expose WeChat operations should adopt the `Wx*` prefix (`WxPay`, `WxConfigProperties`) for discoverability. DTO/VO classes end with `Dto`/`Vo`, enums use singular names, and utility singletons belong in the `fn` package. Leverage `spring-boot-configuration-processor` for strongly typed config—new `@ConfigurationProperties` live in `config` and must register via `spring.factories`.

## Testing Guidelines
Tests rely on `spring-boot-starter-test` (JUnit 5, AssertJ, MockMvc). Name classes `*Test` and mirror the package of the production code to keep Gradle’s default discovery. Cover happy-path parsing plus signature and AES helpers whenever you touch serialization logic. Favor deterministic fixtures over live WeChat calls, and document any network mocks inside the test file.

## Commit & Pull Request Guidelines
Recent history (`fix: callbackfn`, `fix: ticket`) follows the `<type>: <summary>` convention—use lowercase imperative verbs and keep summaries under 60 characters. Reference an issue key when relevant, and avoid bundling unrelated fixes. Pull requests should describe intent, testing done (`mvn test` run), impacted modules, and any screenshots or sample payloads that help reviewers verify behavior.

## Configuration & Security Tips
Sensitive values (app secrets, pay certs) must be injected via external configuration rather than hardcoded. `WxConfigProperties` and `WxPayConfigProperties` already expose the necessary fields; ensure new properties include `@ConstructorBinding` or setters and document expected environment variables. When updating auto-configuration, also update `META-INF/spring.factories` to keep starters discoverable while guarding against accidentally enabling components by default.
