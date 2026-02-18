package org.thingsboard.server.common.data.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemplateUtils - Template variable substitution")
class TemplateUtilsTest {

    @Test
    @DisplayName("Simple variable substitution")
    void testSimpleSubstitution() {
        Map<String, String> context = Map.of("name", "World");

        String result = TemplateUtils.processTemplate("Hello ${name}", context);

        assertThat(result).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("Multiple variable substitutions")
    void testMultipleSubstitutions() {
        Map<String, String> context = Map.of("first", "John", "last", "Doe");

        String result = TemplateUtils.processTemplate("${first} ${last}", context);

        assertThat(result).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Missing variable - should escape and preserve template token")
    void testMissingVariable() {
        Map<String, String> context = Map.of("name", "World");

        String result = TemplateUtils.processTemplate("${name} ${missing}", context);

        assertThat(result).contains("World");
        assertThat(result).contains("${missing}");
    }

    @Test
    @DisplayName("upperCase function - transforms value to uppercase")
    void testUpperCaseFunction() {
        Map<String, String> context = Map.of("name", "hello");

        String result = TemplateUtils.processTemplate("${name:upperCase}", context);

        assertThat(result).isEqualTo("HELLO");
    }

    @Test
    @DisplayName("lowerCase function - transforms value to lowercase")
    void testLowerCaseFunction() {
        Map<String, String> context = Map.of("name", "HELLO");

        String result = TemplateUtils.processTemplate("${name:lowerCase}", context);

        assertThat(result).isEqualTo("hello");
    }

    @Test
    @DisplayName("capitalize function - capitalizes first letter")
    void testCapitalizeFunction() {
        Map<String, String> context = Map.of("name", "hello");

        String result = TemplateUtils.processTemplate("${name:capitalize}", context);

        assertThat(result).isEqualTo("Hello");
    }

    @Test
    @DisplayName("No template variables - returns string unchanged")
    void testNoVariables() {
        Map<String, String> context = Map.of("name", "unused");

        String result = TemplateUtils.processTemplate("Plain text", context);

        assertThat(result).isEqualTo("Plain text");
    }

    @Test
    @DisplayName("Empty context map - all variables treated as missing")
    void testEmptyContext() {
        Map<String, String> context = Map.of();

        String result = TemplateUtils.processTemplate("${name}", context);

        assertThat(result).contains("${name}");
    }

    @Test
    @DisplayName("Null value in context - replaced with empty string")
    void testNullValueInContext() {
        Map<String, String> context = new HashMap<>();
        context.put("name", null);

        String result = TemplateUtils.processTemplate("Hello ${name}!", context);

        assertThat(result).isEqualTo("Hello !");
    }

    @Test
    @DisplayName("Empty value in context - replaced with empty string")
    void testEmptyValueInContext() {
        Map<String, String> context = Map.of("name", "");

        String result = TemplateUtils.processTemplate("Hello ${name}!", context);

        assertThat(result).isEqualTo("Hello !");
    }

    @Test
    @DisplayName("Unknown function - value returned without transformation")
    void testUnknownFunction() {
        Map<String, String> context = Map.of("name", "hello");

        String result = TemplateUtils.processTemplate("${name:unknownFunc}", context);

        assertThat(result).isEqualTo("hello");
    }

    @Test
    @DisplayName("Variable with special regex characters in value")
    void testSpecialRegexCharsInValue() {
        Map<String, String> context = Map.of("path", "C:\\Users\\test");

        String result = TemplateUtils.processTemplate("Path: ${path}", context);

        assertThat(result).isEqualTo("Path: C:\\Users\\test");
    }

    @Test
    @DisplayName("Variable adjacent to text without spaces")
    void testAdjacentText() {
        Map<String, String> context = Map.of("type", "Device");

        String result = TemplateUtils.processTemplate("${type}Profile", context);

        assertThat(result).isEqualTo("DeviceProfile");
    }

    @Test
    @DisplayName("Function with uppercase value - lowerCase transforms correctly")
    void testFunctionChaining() {
        Map<String, String> context = Map.of("type", "MY_DEVICE");

        String result = TemplateUtils.processTemplate("Type: ${type:lowerCase}", context);

        assertThat(result).isEqualTo("Type: my_device");
    }
}
