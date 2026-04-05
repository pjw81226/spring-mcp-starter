package com.jang.mcp.starter.tool.builtin;

import com.jang.mcp.starter.tool.McpToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Built-in MCP tool that fetches the OpenAPI (Swagger) specification from the host application.
 * Requires springdoc-openapi to be available in the host application.
 *
 * 호스트 애플리케이션에서 OpenAPI(Swagger) 스펙을 가져오는 내장 MCP 도구.
 * 호스트 앱에 springdoc-openapi가 설정되어 있어야 한다.
 */
public class ApiSpecMcpTool implements McpToolProvider<Void> {

    private static final Logger log = LoggerFactory.getLogger(ApiSpecMcpTool.class);

    private final String apiDocsUrl;
    private final RestTemplate restTemplate;

    public ApiSpecMcpTool(String apiDocsUrl) {
        this.apiDocsUrl = apiDocsUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getName() {
        return "get_api_spec";
    }

    @Override
    public String getDescription() {
        return "Fetches the OpenAPI (Swagger) specification of the host application in JSON format. Useful for understanding available API endpoints.";
    }

    @Override
    public Class<Void> getParameterType() {
        return null;
    }

    @Override
    public String execute(Void params) {
        try {
            String spec = restTemplate.getForObject(apiDocsUrl, String.class);
            if (spec == null || spec.isBlank()) {
                return "Empty response from API docs endpoint: " + apiDocsUrl;
            }
            return spec;
        } catch (Exception e) {
            log.error("Failed to fetch API spec from: {}", apiDocsUrl, e);
            return "Error fetching API spec: " + e.getMessage();
        }
    }
}

