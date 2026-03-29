# Spring Boot MCP Starter

[![](https://jitpack.io/v/pjw81226/spring-mcp-starter.svg)](https://jitpack.io/#pjw81226/spring-mcp-starter)

**English** | [한국어](README.ko.md)

A lightweight Spring Boot starter that automatically configures an MCP (Model Context Protocol) server with SSE transport.
No Spring AI dependency required -- just the official `mcp-sdk-java`.

---

## Features

- **Zero-config MCP server** -- Add the dependency and it just works.
- **SSE transport** -- Built on the official `HttpServletSseServerTransportProvider` from the MCP Java SDK.
- **Annotation-driven schema** -- Define tool parameters with Java Records and `@McpParameter`. No manual JSON Schema writing.
- **Auto-discovery** -- Any Spring bean implementing `McpToolProvider` is automatically registered as an MCP tool.
- **Built-in tools** -- Log tailing and OpenAPI spec extraction included out of the box.
- **Spring Boot 3.3+ and 4.x compatible**

---

## Requirements

- Java 17+
- Spring Boot 3.3+ (including 4.x)

---

## Quick Start

### 1. Add the dependency

**Gradle**

Add the JitPack repository to your root `settings.gradle`:
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency:
```groovy
dependencies {
    implementation 'com.github.pjw81226:spring-mcp-starter:v0.0.1'
}
```

**Maven**

Add the JitPack repository:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the dependency:
```xml
<dependency>
    <groupId>com.github.pjw81226</groupId>
    <artifactId>spring-mcp-starter</artifactId>
    <version>v0.0.1</version>
</dependency>
```

### 2. Configure `application.yml`

```yaml
mcp:
  server:
    name: my-app
    version: 1.0.0
    # The starter will read from this existing log file for the 'read_backend_log' tool.
    # (Note: This starter does not generate logs, it only reads them)
    log-file-path: logs/application.log      

    # The starter will fetch the existing OpenAPI spec from this URL for the 'get_api_spec' tool.
    # (Note: This starter does not generate Swagger/OpenAPI docs)
    api-docs-url: http://localhost:8080/v3/api-docs  
```

### 3. Create a custom tool

```java
@Component
public class HealthCheckTool implements McpToolProvider {

    public record Params(
        @McpParameter(description = "Target service name to check")
        String serviceName
    ) {}

    @Override
    public String getName() {
        return "health_check";
    }

    @Override
    public String getDescription() {
        return "Checks the health status of a specified service.";
    }

    @Override
    public Class<?> getParameterType() {
        return Params.class;
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        String serviceName = (String) arguments.get("serviceName");
        // your logic here
        return serviceName + " is healthy";
    }
}
```

That's it. The tool is now available to any MCP client (Cursor, Claude Desktop, etc.) at:

```
SSE endpoint:     GET  /mcp/sse
Message endpoint: POST /mcp/message
```

---

## Built-in Tools

These tools are registered automatically when the corresponding property is set.

| Tool name | Trigger property | Description |
|---|---|---|
| `read_backend_log` | `mcp.server.log-file-path` | Reads the last N lines from the application log file (default: 50, max: 500) |
| `get_api_spec` | `mcp.server.api-docs-url` | Fetches the OpenAPI JSON spec from the host application |

---

## Configuration Reference

All properties are under the `mcp.server` prefix.

| Property | Default | Description |
|---|---|---|
| `enabled` | `true` | Enable/disable the MCP server |
| `name` | `spring-mcp-server` | Server name reported to MCP clients |
| `version` | `1.0.0` | Server version reported to MCP clients |
| `base-url` | `/mcp` | Base URL for the MCP servlet |
| `sse-endpoint` | `/sse` | SSE connection endpoint (relative to base-url) |
| `message-endpoint` | `/mcp/message` | Message endpoint for client requests |
| `log-file-path` | _(none)_ | Path to log file. Enables `read_backend_log` tool when set |
| `api-docs-url` | _(none)_ | OpenAPI docs URL. Enables `get_api_spec` tool when set |

---

## Architecture

```
Host Application
  +-- spring-automcp-library (this starter)
        |
        +-- McpAutoConfiguration
        |     +-- Collects all McpToolProvider beans
        |     +-- Generates JSON Schema via McpSchemaGenerator
        |     +-- Registers tools with McpSyncServer
        |
        +-- McpSseTransportFactory
        |     +-- Creates HttpServletSseServerTransportProvider
        |     +-- Registers as Servlet (async-supported)
        |
        +-- Built-in Tools (conditional)
              +-- BackendLogMcpTool   (if log-file-path is set)
              +-- ApiSpecMcpTool      (if api-docs-url is set)
```

```
MCP Client (Cursor, Claude, etc.)
    |
    |  GET /mcp/sse          --> SSE connection established
    |  POST /mcp/message     --> Tool call request / response
    |
Host Application + MCP Server
```

---

## License

[MIT](LICENSE)
