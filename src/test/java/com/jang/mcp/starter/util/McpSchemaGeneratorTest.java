package com.jang.mcp.starter.util;

import com.jang.mcp.starter.annotation.McpParameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class McpSchemaGeneratorTest {

    // -- Test Records --

    public record SimpleParams(
            @McpParameter(description = "User name", required = true)
            String name,
            @McpParameter(description = "User age", required = false)
            Integer age
    ) {}

    public record AllTypesParams(
            @McpParameter(description = "a string") String text,
            @McpParameter(description = "an int") int intVal,
            @McpParameter(description = "an integer") Integer integerVal,
            @McpParameter(description = "a long") long longVal,
            @McpParameter(description = "a Long") Long longWrapperVal,
            @McpParameter(description = "a float") float floatVal,
            @McpParameter(description = "a double") double doubleVal,
            @McpParameter(description = "a boolean") boolean boolVal,
            @McpParameter(description = "a Boolean") Boolean boolWrapperVal
    ) {}

    public record NoAnnotationParams(
            String username,
            int count
    ) {}

    public record ListParam(
            @McpParameter(description = "tags list")
            List<String> tags
    ) {}

    // -- Tests --

    @Test
    @DisplayName("null parameterType returns empty schema")
    void nullParameterType() {
        Map<String, Object> schema = McpSchemaGenerator.generate(null);

        assertEquals("object", schema.get("type"));
        assertNotNull(schema.get("properties"));
        assertTrue(((Map<?, ?>) schema.get("properties")).isEmpty());
        assertNull(schema.get("required"));
    }

    @Test
    @DisplayName("Record with @McpParameter generates correct properties and required list")
    @SuppressWarnings("unchecked")
    void simpleParamsSchema() {
        Map<String, Object> schema = McpSchemaGenerator.generate(SimpleParams.class);

        assertEquals("object", schema.get("type"));

        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties);
        assertEquals(2, properties.size());

        // name field
        Map<String, Object> nameField = (Map<String, Object>) properties.get("name");
        assertEquals("string", nameField.get("type"));
        assertEquals("User name", nameField.get("description"));

        // age field
        Map<String, Object> ageField = (Map<String, Object>) properties.get("age");
        assertEquals("integer", ageField.get("type"));
        assertEquals("User age", ageField.get("description"));

        // required - only "name" (age is required=false)
        List<String> required = (List<String>) schema.get("required");
        assertNotNull(required);
        assertEquals(1, required.size());
        assertTrue(required.contains("name"));
        assertFalse(required.contains("age"));
    }

    @Test
    @DisplayName("All Java types are correctly mapped to JSON Schema types")
    @SuppressWarnings("unchecked")
    void allTypesMapping() {
        Map<String, Object> schema = McpSchemaGenerator.generate(AllTypesParams.class);
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

        assertEquals("string", ((Map<String, Object>) properties.get("text")).get("type"));
        assertEquals("integer", ((Map<String, Object>) properties.get("intVal")).get("type"));
        assertEquals("integer", ((Map<String, Object>) properties.get("integerVal")).get("type"));
        assertEquals("integer", ((Map<String, Object>) properties.get("longVal")).get("type"));
        assertEquals("integer", ((Map<String, Object>) properties.get("longWrapperVal")).get("type"));
        assertEquals("number", ((Map<String, Object>) properties.get("floatVal")).get("type"));
        assertEquals("number", ((Map<String, Object>) properties.get("doubleVal")).get("type"));
        assertEquals("boolean", ((Map<String, Object>) properties.get("boolVal")).get("type"));
        assertEquals("boolean", ((Map<String, Object>) properties.get("boolWrapperVal")).get("type"));
    }

    @Test
    @DisplayName("Fields without @McpParameter are still included but without description")
    @SuppressWarnings("unchecked")
    void noAnnotationFields() {
        Map<String, Object> schema = McpSchemaGenerator.generate(NoAnnotationParams.class);
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

        assertEquals(2, properties.size());

        Map<String, Object> usernameField = (Map<String, Object>) properties.get("username");
        assertEquals("string", usernameField.get("type"));
        assertNull(usernameField.get("description"));

        // No required list since no @McpParameter annotation
        assertNull(schema.get("required"));
    }

    @Test
    @DisplayName("List type fields are mapped to 'array'")
    @SuppressWarnings("unchecked")
    void listTypeMapping() {
        Map<String, Object> schema = McpSchemaGenerator.generate(ListParam.class);
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

        Map<String, Object> tagsField = (Map<String, Object>) properties.get("tags");
        assertEquals("array", tagsField.get("type"));
    }
}
