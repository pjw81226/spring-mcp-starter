package com.jang.mcp.starter.tool.builtin;

import com.jang.mcp.starter.annotation.McpParameter;
import com.jang.mcp.starter.tool.McpToolProvider;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Built-in MCP tool that reads the tail of the application log file.
 * Useful for AI agents to diagnose runtime errors and monitor application behavior.
 *
 * 애플리케이션 로그 파일의 마지막 N줄을 읽는 내장 MCP 도구.
 * AI 에이전트가 런타임 에러를 진단하고 애플리케이션 동작을 모니터링하는 데 유용하다.
 */
public class BackendLogMcpTool implements McpToolProvider<BackendLogMcpTool.Params> {

    private static final Logger log = LoggerFactory.getLogger(BackendLogMcpTool.class);
    private static final int DEFAULT_LINES = 50;
    private static final int MAX_LINES = 500;

    private final String logFilePath;

    public BackendLogMcpTool(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * Parameter definition for the log tailing tool.
     *
     * 로그 테일링 도구의 파라미터 정의.
     */
    public record Params(
            @McpParameter(description = "Number of lines to read from the end of the log file (default: 50, max: 500)", required = false)
            Integer lines
    ) {}

    @Override
    public String getName() {
        return "read_backend_log";
    }

    @Override
    public String getDescription() {
        return "Reads the last N lines from the application log file. Useful for diagnosing errors and monitoring runtime behavior.";
    }

    @Override
    public Class<Params> getParameterType() {
        return Params.class;
    }

    @Override
    public String execute(Params params) {
        int lines = DEFAULT_LINES;
        if (params != null && params.lines() != null) {
            lines = params.lines();
        }
        lines = Math.min(Math.max(lines, 1), MAX_LINES);

        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            return "Log file not found: " + logFilePath;
        }

        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setFile(logFile)
                .setCharset(StandardCharsets.UTF_8)
                .get()) {

            List<String> result = new ArrayList<>();
            for (int i = 0; i < lines; i++) {
                String line = reader.readLine();
                if (line == null) break;
                result.add(line);
            }

            // Reverse to maintain chronological order
            // 시간순으로 정렬하기 위해 역순으로 뒤집는다
            Collections.reverse(result);
            return String.join("\n", result);

        } catch (Exception e) {
            log.error("Failed to read log file: {}", logFilePath, e);
            return "Error reading log file: " + e.getMessage();
        }
    }
}
