package com.jang.mcp.starter.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jang.mcp.starter.annotation.McpParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ArgumentConverter.convert() — the production argument conversion logic.
 */
class ArgumentConverterTest {

    private ObjectMapper objectMapper;

    public record TestParams(
            @McpParameter(description = "name") String name,
            @McpParameter(description = "count") Integer count
    ) {}

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // -- Parameterless tool behavior --

    @Test
    @DisplayName("null paramType returns null")
    void nullParamTypeReturnsNull() {
        Object result = ArgumentConverter.convert(Map.of("key", "value"), null, objectMapper);
        assertNull(result);
    }

    @Test
    @DisplayName("Void paramType returns null")
    void voidParamTypeReturnsNull() {
        Object result = ArgumentConverter.convert(Map.of("key", "value"), Void.class, objectMapper);
        assertNull(result);
    }

    // -- Valid conversion --

    @Test
    @DisplayName("Valid arguments are converted to typed object")
    void validConversion() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Alice");
        args.put("count", 42);

        Object result = ArgumentConverter.convert(args, TestParams.class, objectMapper);

        assertInstanceOf(TestParams.class, result);
        TestParams params = (TestParams) result;
        assertEquals("Alice", params.name());
        assertEquals(42, params.count());
    }

    @Test
    @DisplayName("Partial arguments convert with null for missing fields")
    void partialArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Bob");

        Object result = ArgumentConverter.convert(args, TestParams.class, objectMapper);

        assertInstanceOf(TestParams.class, result);
        TestParams params = (TestParams) result;
        assertEquals("Bob", params.name());
        assertNull(params.count());
    }

    @Test
    @DisplayName("Empty map converts to object with all null fields")
    void emptyArguments() {
        Map<String, Object> args = new HashMap<>();

        Object result = ArgumentConverter.convert(args, TestParams.class, objectMapper);

        assertInstanceOf(TestParams.class, result);
        TestParams params = (TestParams) result;
        assertNull(params.name());
        assertNull(params.count());
    }

    // -- Error cases --

    @Test
    @DisplayName("null arguments with non-Void paramType throws IllegalArgumentException")
    void nullArgumentsThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ArgumentConverter.convert(null, TestParams.class, objectMapper));

        assertTrue(ex.getMessage().contains("Missing arguments"));
        assertTrue(ex.getMessage().contains("TestParams"));
    }

    @Test
    @DisplayName("Invalid type in arguments throws conversion exception")
    void invalidTypeThrows() {
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Alice");
        args.put("count", "not-a-number");

        assertThrows(IllegalArgumentException.class, () ->
                ArgumentConverter.convert(args, TestParams.class, objectMapper));
    }
}
