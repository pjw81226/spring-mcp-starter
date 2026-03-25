# Spring Boot MCP Starter

[English](README.md) | **한국어**

Spring Boot 애플리케이션에 추가하기만 하면 MCP(Model Context Protocol) 서버를 SSE 방식으로 자동 구성해주는 초경량 스타터 라이브러리입니다.
Spring AI 의존성 없이 공식 `mcp-sdk-java`만 사용합니다.

---

## 주요 기능

- **설정 없이 MCP 서버 구동** -- 의존성만 추가하면 바로 동작합니다.
- **SSE 기반 통신** -- MCP Java SDK의 `HttpServletSseServerTransportProvider`를 사용합니다.
- **어노테이션 기반 스키마** -- Java Record와 `@McpParameter`로 도구 파라미터를 정의합니다. JSON Schema를 직접 작성할 필요가 없습니다.
- **자동 수집** -- `McpToolProvider` 인터페이스를 구현한 Spring Bean은 자동으로 MCP 서버에 등록됩니다.
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
```groovy
dependencies {
    implementation 'com.jang.mcp:spring-automcp-library:0.0.1-SNAPSHOT'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.jang.mcp</groupId>
    <artifactId>spring-automcp-library</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. `application.yml` 설정

```yaml
mcp:
  server:
    name: my-app
    version: 1.0.0
    log-file-path: logs/application.log          # 로그 파일의 경로 (직접 생성 후 경로 설정해주셔야합니다.)
    api-docs-url: http://localhost:8080/v3/api-docs  # API 문서 경로 (마찬가지로 직접 생성후 설정해주셔야합니다.)
```

### 3. 커스텀 도구 만들기

```java
@Component
public class HealthCheckTool implements McpToolProvider {

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
    public Class<?> getParameterType() {
        return Params.class;
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        String serviceName = (String) arguments.get("serviceName");
        // 여기에 실제 로직 작성
        return serviceName + " is healthy";
    }
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

## 라이선스

[MIT](LICENSE)
