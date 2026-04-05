package com.jang.mcp.starter.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Utility for converting raw MCP tool arguments into typed parameter objects.
 *
 * MCP 도구의 원시 인자를 타입이 지정된 파라미터 객체로 변환하는 유틸리티.
 */
public final class ArgumentConverter {

    private ArgumentConverter() {
    }

    /**
     * Converts raw argument map to the typed parameter object.
     * Returns null for parameterless tools (Void type or null paramType).
     * Throws IllegalArgumentException if arguments are missing for a parameterized tool.
     *
     * 원시 인자 맵을 타입이 지정된 파라미터 객체로 변환한다.
     * 파라미터가 없는 도구(Void 또는 null)에는 null을 반환한다.
     * 파라미터가 필요한 도구에 인자가 누락되면 IllegalArgumentException을 던진다.
     *
     * @param arguments  raw argument map from MCP client
     * @param paramType  target parameter type class
     * @param objectMapper  Jackson ObjectMapper for conversion
     * @return converted parameter object, or null for parameterless tools
     */
    public static Object convert(Map<String, Object> arguments, Class<?> paramType, ObjectMapper objectMapper) {
        if (paramType == null || paramType == Void.class) {
            return null;
        }
        if (arguments == null) {
            throw new IllegalArgumentException("Missing arguments for tool that expects parameters of type " + paramType.getSimpleName());
        }
        return objectMapper.convertValue(arguments, paramType);
    }
}
