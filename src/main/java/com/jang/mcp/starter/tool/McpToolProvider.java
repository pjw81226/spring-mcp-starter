package com.jang.mcp.starter.tool;

/**
 * Common interface for defining MCP tools.
 * Spring beans implementing this interface are automatically registered to the MCP server.
 * The type parameter T represents the tool's parameter type (use a Java Record).
 * For tools with no parameters, use {@code Void} as the type parameter.
 *
 * MCP 도구를 정의하기 위한 공통 인터페이스.
 * 이 인터페이스를 구현한 스프링 빈은 자동으로 MCP 서버에 등록된다.
 * 타입 파라미터 T는 도구의 파라미터 타입을 나타낸다 (Java Record 사용 권장).
 * 파라미터가 없는 도구는 {@code Void}를 타입 파라미터로 사용한다.
 *
 * @param <T> the parameter type for this tool, or {@code Void} if no parameters
 */
public interface McpToolProvider<T> {

    /**
     * Unique name of the tool. MCP clients invoke the tool using this name.
     * 
     * 도구의 고유 이름. MCP 클라이언트가 이 이름으로 도구를 호출한다.
     */
    String getName();

    /**
     * Description of the tool. The LLM reads this to determine the tool's purpose.
     * 
     * 도구에 대한 설명. LLM이 이 설명을 보고 도구의 용도를 판단한다.
     */
    String getDescription();

    /**
     * Returns the Record class that defines the tool's parameters.
     * Override this method to specify the parameter type for your tool.
     * Returns null by default for parameterless tools.
     * Apply @McpParameter to the Record's fields to add descriptions.
     * 
     * 도구의 파라미터를 정의하는 Record 클래스를 반환한다.
     * 파라미터가 있는 도구는 이 메서드를 오버라이드하여 타입을 지정한다.
     * 파라미터가 없는 도구는 기본값(null)을 사용한다.
     * Record의 필드에 @McpParameter를 적용하여 설명을 추가한다.
     */
    default Class<T> getParameterType() {
        return null;
    }

    /**
     * Executes the tool with the typed parameter object.
     * The framework automatically converts raw JSON arguments into the specified type.
     * For tools with no parameters (Void), this method receives null.
     *
     * 타입이 지정된 파라미터 객체를 받아 도구를 실행한다.
     * 프레임워크가 JSON 인자를 지정된 타입으로 자동 변환한다.
     * 파라미터가 없는 도구(Void)는 null을 받는다.
     *
     * @param params typed parameter object, or null for parameterless tools
     * @return result string delivered to the LLM as text
     */
    String execute(T params);
}

