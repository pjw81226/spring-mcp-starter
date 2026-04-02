package com.jang.mcp.starter.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jang.mcp.starter.controller.McpSseTransportFactory;
import com.jang.mcp.starter.tool.McpToolProvider;
import com.jang.mcp.starter.tool.builtin.ApiSpecMcpTool;
import com.jang.mcp.starter.tool.builtin.BackendLogMcpTool;
import com.jang.mcp.starter.util.McpSchemaGenerator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;

/**
 * Spring Boot Auto-Configuration that wires up the MCP server.
 * Collects all McpToolProvider beans, generates JSON schemas, and registers tools
 * with the MCP server via SSE transport.
 * Uses ObjectMapper to automatically convert raw JSON arguments into typed parameter objects.
 *
 * MCP 서버를 자동 구성하는 스프링 부트 Auto-Configuration.
 * 모든 McpToolProvider 빈을 수집하고, JSON 스키마를 생성하고,
 * SSE Transport를 통해 MCP 서버에 도구를 등록한다.
 * ObjectMapper를 사용하여 JSON 인자를 타입이 지정된 파라미터 객체로 자동 변환한다.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "mcp.server", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(McpProperties.class)
public class McpAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(McpAutoConfiguration.class);

    /**
     * Creates the SSE transport provider.
     *
     * SSE Transport Provider를 생성한다.
     */
    @Bean
    public HttpServletSseServerTransportProvider mcpTransportProvider(McpProperties properties) {
        return McpSseTransportFactory.createTransportProvider(
                properties.getBaseUrl(),
                properties.getSseEndpoint(),
                properties.getMessageEndpoint()
        );
    }

    /**
     * Registers the transport provider as a Servlet.
     *
     * Transport Provider를 서블릿으로 등록한다.
     */
    @Bean
    public ServletRegistrationBean<HttpServletSseServerTransportProvider> mcpServletRegistration(
            HttpServletSseServerTransportProvider transportProvider, McpProperties properties) {
        return McpSseTransportFactory.createServletRegistration(transportProvider, properties.getBaseUrl());
    }

    /**
     * Registers the built-in BackendLogMcpTool if logFilePath is configured.
     *
     * logFilePath가 설정되어 있으면 내장 BackendLogMcpTool을 등록한다.
     */
    @Bean
    @ConditionalOnProperty(prefix = "mcp.server", name = "log-file-path")
    public BackendLogMcpTool backendLogMcpTool(McpProperties properties) {
        log.info("Registering built-in MCP tool: read_backend_log (logFilePath={})", properties.getLogFilePath());
        return new BackendLogMcpTool(properties.getLogFilePath());
    }

    /**
     * Registers the built-in ApiSpecMcpTool if apiDocsUrl is configured.
     *
     * apiDocsUrl이 설정되어 있으면 내장 ApiSpecMcpTool을 등록한다.
     */
    @Bean
    @ConditionalOnProperty(prefix = "mcp.server", name = "api-docs-url")
    public ApiSpecMcpTool apiSpecMcpTool(McpProperties properties) {
        log.info("Registering built-in MCP tool: get_api_spec (apiDocsUrl={})", properties.getApiDocsUrl());
        return new ApiSpecMcpTool(properties.getApiDocsUrl());
    }

    /**
     * Creates and starts the MCP sync server.
     * Collects all McpToolProvider beans, converts their parameters to JSON Schema,
     * and registers them as MCP tools.
     * Raw JSON arguments are automatically converted to typed objects via ObjectMapper.
     *
     * MCP 동기 서버를 생성하고 시작한다.
     * 모든 McpToolProvider 빈을 수집하고, 파라미터를 JSON Schema로 변환하여 MCP 도구로 등록한다.
     * ObjectMapper를 통해 JSON 인자를 타입 객체로 자동 변환한다.
     */
    @Bean
    @SuppressWarnings({"unchecked", "rawtypes"})
    public McpSyncServer mcpSyncServer(
            HttpServletSseServerTransportProvider transportProvider,
            List<McpToolProvider<?>> toolProviders,
            ObjectMapper objectMapper,
            McpProperties properties) {

        var serverSpec = McpServer.sync(transportProvider)
                .serverInfo(properties.getName(), properties.getVersion())
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(false)
                        .build());

        for (McpToolProvider provider : toolProviders) {
            Map<String, Object> schemaMap = McpSchemaGenerator.generate(provider.getParameterType());

            McpSchema.JsonSchema inputSchema = new McpSchema.JsonSchema(
                    (String) schemaMap.get("type"),
                    (Map<String, Object>) schemaMap.get("properties"),
                    (List<String>) schemaMap.get("required"),
                    null, null, null
            );

            McpSchema.Tool tool = McpSchema.Tool.builder()
                    .name(provider.getName())
                    .description(provider.getDescription())
                    .inputSchema(inputSchema)
                    .build();

            Class<?> paramType = provider.getParameterType();

            serverSpec.toolCall(tool, (exchange, request) -> {
                try {
                    Object params = convertArguments(request.arguments(), paramType, objectMapper);
                    String result = provider.execute(params);
                    return McpSchema.CallToolResult.builder()
                            .addTextContent(result)
                            .build();
                } catch (Exception e) {
                    log.error("Tool execution failed: {}", provider.getName(), e);
                    return McpSchema.CallToolResult.builder()
                            .addTextContent("Error: " + e.getMessage())
                            .isError(true)
                            .build();
                }
            });

            log.info("Registered MCP tool: {} - {}", provider.getName(), provider.getDescription());
        }

        McpSyncServer server = serverSpec.build();
        log.info("MCP server started: {} v{} with {} tools at {}",
                properties.getName(), properties.getVersion(),
                toolProviders.size(), properties.getBaseUrl() + properties.getSseEndpoint());

        return server;
    }

    /**
     * Converts raw argument map to the typed parameter object.
     * Returns null for parameterless tools (Void type or null paramType).
     *
     * 원시 인자 맵을 타입이 지정된 파라미터 객체로 변환한다.
     * 파라미터가 없는 도구(Void 또는 null)에는 null을 반환한다.
     */
    private Object convertArguments(Map<String, Object> arguments, Class<?> paramType, ObjectMapper objectMapper) {
        if (paramType == null || paramType == Void.class) {
            return null;
        }
        return objectMapper.convertValue(arguments, paramType);
    }
}
