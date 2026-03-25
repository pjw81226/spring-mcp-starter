package com.jang.mcp.starter.controller;

import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

/**
 * Factory that creates and registers the MCP SSE transport as a Servlet.
 * The HttpServletSseServerTransportProvider handles both SSE and message endpoints internally.
 *
 * MCP SSE Transport를 서블릿으로 생성하고 등록하는 팩토리.
 * HttpServletSseServerTransportProvider가 SSE와 메시지 엔드포인트를 내부적으로 처리한다.
 */
public final class McpSseTransportFactory {

    private McpSseTransportFactory() {
    }

    /**
     * Default SSE endpoint path.
     *
     * 기본 SSE 엔드포인트 경로.
     */
    public static final String DEFAULT_SSE_ENDPOINT = "/sse";

    /**
     * Default message endpoint path.
     *
     * 기본 메시지 엔드포인트 경로.
     */
    public static final String DEFAULT_MESSAGE_ENDPOINT = "/mcp/message";

    /**
     * Default base URL prefix for the MCP servlet mapping.
     *
     * MCP 서블릿 매핑을 위한 기본 베이스 URL 접두사.
     */
    public static final String DEFAULT_BASE_URL = "/mcp";

    /**
     * Creates an HttpServletSseServerTransportProvider with the given configuration.
     *
     * 주어진 설정으로 HttpServletSseServerTransportProvider를 생성한다.
     *
     * @param baseUrl base URL for the MCP servlet (e.g. "/mcp")
     * @param sseEndpoint SSE connection endpoint path (e.g. "/sse")
     * @param messageEndpoint message receiving endpoint path (e.g. "/mcp/message")
     * @return configured transport provider
     */
    public static HttpServletSseServerTransportProvider createTransportProvider(
            String baseUrl, String sseEndpoint, String messageEndpoint) {
        return HttpServletSseServerTransportProvider.builder()
                .baseUrl(baseUrl)
                .sseEndpoint(sseEndpoint)
                .messageEndpoint(messageEndpoint)
                .build();
    }

    /**
     * Registers the transport provider as a Servlet under the specified URL pattern.
     * Clients connect to {baseUrl}/sse for SSE and {baseUrl}/mcp/message for messages.
     *
     * Transport Provider를 지정된 URL 패턴으로 서블릿으로 등록한다.
     * 클라이언트는 {baseUrl}/sse로 SSE 연결, {baseUrl}/mcp/message로 메시지를 전송한다.
     *
     * @param transportProvider the MCP SSE transport provider
     * @param baseUrl the servlet mapping base URL (e.g. "/mcp")
     * @return configured ServletRegistrationBean
     */
    public static ServletRegistrationBean<HttpServletSseServerTransportProvider> createServletRegistration(
            HttpServletSseServerTransportProvider transportProvider, String baseUrl) {
        ServletRegistrationBean<HttpServletSseServerTransportProvider> registration =
                new ServletRegistrationBean<>(transportProvider, baseUrl + "/*");
        registration.setName("mcpSseTransport");
        registration.setAsyncSupported(true);
        return registration;
    }
}
