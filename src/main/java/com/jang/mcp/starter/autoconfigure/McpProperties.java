package com.jang.mcp.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the MCP server.
 * Bind to properties prefixed with "mcp.server" in application.yml/properties.
 *
 * MCP 서버의 설정 프로퍼티.
 * application.yml/properties에서 "mcp.server" 접두사로 바인딩된다.
 */
@ConfigurationProperties(prefix = "mcp.server")
public class McpProperties {

    /**
     * Whether to enable the MCP server. Default: true
     *
     * MCP 서버 활성화 여부. 기본값: true
     */
    private boolean enabled = true;

    /**
     * Server name reported to MCP clients during initialization.
     *
     * 초기화 시 MCP 클라이언트에 전달되는 서버 이름.
     */
    private String name = "spring-mcp-server";

    /**
     * Server version reported to MCP clients.
     *
     * MCP 클라이언트에 전달되는 서버 버전.
     */
    private String version = "1.0.0";

    /**
     * Base URL prefix for the MCP servlet mapping (e.g. "/mcp").
     *
     * MCP 서블릿 매핑의 베이스 URL 접두사.
     */
    private String baseUrl = "/mcp";

    /**
     * SSE connection endpoint path (relative to servlet context).
     *
     * SSE 연결 엔드포인트 경로 (서블릿 컨텍스트 기준 상대 경로).
     */
    private String sseEndpoint = "/sse";

    /**
     * Message endpoint path for client-to-server requests.
     *
     * 클라이언트→서버 메시지 엔드포인트 경로.
     */
    private String messageEndpoint = "/mcp/message";

    /**
     * Path to the application log file for the built-in log tailing tool.
     * If not set, the BackendLogMcpTool will not be registered.
     *
     * 내장 로그 테일링 도구용 로그 파일 경로.
     * 설정하지 않으면 BackendLogMcpTool이 등록되지 않는다.
     */
    private String logFilePath;

    /**
     * URL for the OpenAPI docs endpoint (e.g. "http://localhost:8080/v3/api-docs").
     * If not set, the ApiSpecMcpTool will not be registered.
     *
     * OpenAPI 문서 엔드포인트 URL.
     * 설정하지 않으면 ApiSpecMcpTool이 등록되지 않는다.
     */
    private String apiDocsUrl;

    /**
     * Global timeout for tool execution. Default: 30 seconds.
     * If a tool does not complete within this duration, the execution is cancelled
     * and an error is returned to the MCP client.
     * Individual tools can override this via {@code McpToolProvider.getTimeout()}.
     *
     * 도구 실행의 글로벌 타임아웃. 기본값: 30초.
     * 이 시간 내에 도구가 완료되지 않으면 실행이 취소되고 MCP 클라이언트에 에러가 반환된다.
     * 개별 도구는 {@code McpToolProvider.getTimeout()}으로 오버라이드할 수 있다.
     */
    private Duration toolTimeout = Duration.ofSeconds(30);

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSseEndpoint() {
        return sseEndpoint;
    }

    public void setSseEndpoint(String sseEndpoint) {
        this.sseEndpoint = sseEndpoint;
    }

    public String getMessageEndpoint() {
        return messageEndpoint;
    }

    public void setMessageEndpoint(String messageEndpoint) {
        this.messageEndpoint = messageEndpoint;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getApiDocsUrl() {
        return apiDocsUrl;
    }

    public void setApiDocsUrl(String apiDocsUrl) {
        this.apiDocsUrl = apiDocsUrl;
    }

    public Duration getToolTimeout() {
        return toolTimeout;
    }

    public void setToolTimeout(Duration toolTimeout) {
        this.toolTimeout = toolTimeout;
    }
}
