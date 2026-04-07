# Spring Boot MCP Starter

[![JitPack](https://jitpack.io/v/pjw81226/spring-mcp-starter.svg)](https://jitpack.io/#pjw81226/spring-mcp-starter)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://github.com/pjw81226/spring-mcp-starter)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[English](README.md) | **한국어**

Spring Boot 애플리케이션에 추가하기만 하면 MCP(Model Context Protocol) 서버를 SSE 방식으로 자동 구성해주는 초경량 스타터 라이브러리입니다.
Spring AI 의존성 없이 공식 `mcp-sdk-java`만 사용합니다.

---

## 주요 기능

- **설정 없이 MCP 서버 구동** -- 의존성만 추가하면 바로 동작합니다.
- **SSE 기반 통신** -- MCP Java SDK의 `HttpServletSseServerTransportProvider`를 사용합니다.
- **어노테이션 기반 스키마** -- Java Record와 `@McpParameter`로 도구 파라미터를 정의합니다. JSON Schema를 직접 작성할 필요가 없습니다.
- **타입 안전한 파라미터** -- 제네릭 `McpToolProvider<T>` 인터페이스로 JSON 인자를 자동 변환합니다. `Map` 수동 캐스팅이 필요 없습니다.
- **자동 수집** -- `McpToolProvider<T>` 인터페이스를 구현한 Spring Bean은 자동으로 MCP 서버에 등록됩니다.
- **내장 도구 제공** -- 로그 테일링, OpenAPI 스펙 추출 도구가 기본 포함되어 있습니다.
- **Spring Boot 3.3+ 및 4.x 호환**

---

## 요구사항

- Java 17+
- Spring Boot 3.3+ (4.x 포함)

---

## 빠른 시작

### 1. 의존성 추가

**Gradle**

루트 `settings.gradle`에 JitPack 저장소를 추가합니다:
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

그 다음 의존성을 추가합니다:
```groovy
dependencies {
    implementation 'com.github.pjw81226:spring-mcp-starter:v0.0.2'
}
```

**Maven**

JitPack 저장소를 추가합니다:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

그 다음 의존성을 추가합니다:
```xml
<dependency>
    <groupId>com.github.pjw81226</groupId>
    <artifactId>spring-mcp-starter</artifactId>
    <version>v0.0.2</version>
</dependency>
```

### 2. `application.yml` 설정

```yaml
mcp:
  server:
    name: my-app
    version: 1.0.0
    # 스타터가 'read_backend_log' 도구를 위해 이 '기존' 로그 파일을 읽어옵니다.
    # (참고: 이 라이브러리는 로그를 새로 생성하지 않으며, 오직 읽기만 합니다. 로그파일을 생성하는 설정을 따로 해주셔야합니다.)
    log-file-path: logs/application.log          

    # 스타터가 'get_api_spec' 도구를 위해 이 URL에서 '기존' OpenAPI 스펙을 가져옵니다.
    # (참고: 이 라이브러리는 Swagger/OpenAPI 문서를 생성하지 않습니다. 별도의 설정을 통해 생성 후 경로를 설정해주셔야합니다.)
    api-docs-url: http://localhost:8080/v3/api-docs  
```

### 3. 커스텀 도구 만들기

```java
// 파라미터가 있는 도구
@Component
public class HealthCheckTool implements McpToolProvider<HealthCheckTool.Params> {

    public record Params(
        @McpParameter(description = "상태를 확인할 대상 서비스 이름")
        String serviceName
    ) {}

    @Override
    public String getName() {
        return "health_check";
    }

    @Override
    public String getDescription() {
        return "지정된 서비스의 상태를 확인합니다.";
    }

    @Override
    public Class<Params> getParameterType() {
        return Params.class;
    }

    @Override
    public String execute(Params params) {
        // 타입 안전 — 캐스팅 불필요
        return params.serviceName() + " is healthy";
    }
}

// 파라미터 없는 도구 — getParameterType() 오버라이드 불필요
@Component
public class PingTool implements McpToolProvider<Void> {

    @Override
    public String getName() { return "ping"; }

    @Override
    public String getDescription() { return "pong을 반환합니다."; }

    @Override
    public String execute(Void params) { return "pong"; }
}
```

이것으로 끝입니다. 도구는 MCP 클라이언트(Cursor, Claude Desktop 등)에서 바로 사용할 수 있습니다:

```
SSE 엔드포인트:    GET  /mcp/sse
메시지 엔드포인트: POST /mcp/message
```

---

## 내장 도구

해당 프로퍼티가 설정되면 자동으로 등록됩니다.

| 도구 이름 | 활성화 프로퍼티 | 설명 |
|---|---|---|
| `read_backend_log` | `mcp.server.log-file-path` | 애플리케이션 로그 파일의 마지막 N줄을 읽습니다 (기본: 50, 최대: 500) |
| `get_api_spec` | `mcp.server.api-docs-url` | 호스트 앱의 OpenAPI JSON 스펙을 가져옵니다 |

---

## 설정 레퍼런스

모든 프로퍼티는 `mcp.server` 접두사 아래에 있습니다.

| 프로퍼티 | 기본값 | 설명 |
|---|---|---|
| `enabled` | `true` | MCP 서버 활성화/비활성화 |
| `name` | `spring-mcp-server` | MCP 클라이언트에 보고되는 서버 이름 |
| `version` | `1.0.0` | MCP 클라이언트에 보고되는 서버 버전 |
| `base-url` | `/mcp` | MCP 서블릿의 베이스 URL |
| `sse-endpoint` | `/sse` | SSE 연결 엔드포인트 (base-url 기준 상대 경로) |
| `message-endpoint` | `/mcp/message` | 클라이언트 요청을 수신하는 메시지 엔드포인트 |
| `log-file-path` | _(없음)_ | 로그 파일 경로. 설정 시 `read_backend_log` 도구가 활성화됩니다 |
| `api-docs-url` | _(없음)_ | OpenAPI 문서 URL. 설정 시 `get_api_spec` 도구가 활성화됩니다 |

---

## 아키텍처

```
호스트 애플리케이션
  +-- spring-automcp-library (이 스타터)
        |
        +-- McpAutoConfiguration
        |     +-- 모든 McpToolProvider 빈 수집
        |     +-- McpSchemaGenerator로 JSON Schema 생성
        |     +-- McpSyncServer에 도구 등록
        |
        +-- McpSseTransportFactory
        |     +-- HttpServletSseServerTransportProvider 생성
        |     +-- 서블릿으로 등록 (비동기 지원)
        |
        +-- 내장 도구 (조건부)
              +-- BackendLogMcpTool   (log-file-path 설정 시)
              +-- ApiSpecMcpTool      (api-docs-url 설정 시)
```

```
MCP 클라이언트 (Cursor, Claude 등)
    |
    |  GET /mcp/sse          --> SSE 연결 수립
    |  POST /mcp/message     --> 도구 호출 요청 / 응답
    |
호스트 애플리케이션 + MCP 서버
```

---

## 예제

커스텀 도구(`get_server_info`, `get_db_schema`, `get_spring_beans`)가 포함된 전체 동작 예제는 [example 프로젝트](https://github.com/pjw81226/spring-mcp-starter/tree/example-code/example)를 참고하세요.

---

## 트러블슈팅

### MCP 메시지 엔드포인트에서 `NoSuchFieldError: POJO` 발생

**증상**: SSE 연결(`/mcp/sse`)은 정상이지만, 메시지 엔드포인트에 POST 요청 시 HTTP 500 에러 발생:
```
java.lang.NoSuchFieldError: POJO
  at tools.jackson.databind.deser.DeserializerCache._createDeserializer2(...)
```

**원인**: MCP Java SDK (v1.1.1)가 Jackson 3.x를 사용하며, 이 라이브러리는 `jackson-annotations:2.20`을 필요로 합니다.
그런데 Spring Boot BOM(`io.spring.dependency-management` 플러그인)이 `jackson-annotations`를 `2.18.x`로 강제 다운그레이드하여 호환성 문제가 발생합니다.

**해결**: `build.gradle`에 `jackson-annotations:2.20`을 명시적으로 선언하여 BOM을 오버라이드합니다:

```groovy
dependencies {
    implementation 'com.github.pjw81226:spring-mcp-starter:v0.0.2'

    // 필수: Spring Boot BOM의 jackson-annotations 버전을 오버라이드
    implementation 'com.fasterxml.jackson.core:jackson-annotations:2.20'
}
```

---

## 라이선스

[MIT](LICENSE)
