package test.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.vavr.collection.Array;
import org.assertj.core.api.AbstractAssert;
import org.assertj.vavr.api.VavrAssertions;

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NUMBER;
import static com.fasterxml.jackson.databind.node.JsonNodeType.OBJECT;
import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;

/**
 * A simple custom AssertJ assertion class for handling basic {@link JsonNode} assertions.
 */
public class JsonNodeAssert extends AbstractAssert<JsonNodeAssert, JsonNode> {

    private JsonNodeAssert(JsonNode actual) {
        super(actual, JsonNodeAssert.class);
    }

    public static JsonNodeAssert assertThat(JsonNode actual) {
        return new JsonNodeAssert(actual);
    }

    public JsonNodeAssert hasTextEqualTo(String expectedTemplate, Object... args) {
        return hasTextEqualTo(String.format(expectedTemplate, args));
    }

    public JsonNodeAssert hasTextEqualTo(String expected) {
        assertNodeIsOfType(STRING);
        if (!Objects.equals(actual.asText(), expected)) {
            failWithMessage("Expected JsonNode's text to be <%s> but was <%s>", expected, actual.asText());
        }
        return this;
    }

    public JsonNodeAssert hasTextContaining(String expectedTemplate, Object... args) {
        return hasTextContaining(String.format(expectedTemplate, args));
    }

    public JsonNodeAssert hasTextContaining(final String expected) {
        assertNodeIsOfType(STRING);
        if (!actual.asText().contains(expected)) {
            failWithMessage("Expected JsonNode's text to contain <%s> but was <%s>", expected, actual.asText());
        }
        return this;
    }

    /**
     * Check that the given text is contained and is positioned either at the beginning/end of the string, or surrounded by spaces
     * <p>
     * Examples:
     * "A B C".hasSeparatedTextContaining("A"); // PASS
     * "A B C".hasSeparatedTextContaining("B"); // PASS
     * "A B C".hasSeparatedTextContaining("C"); // PASS
     * "A  BC".hasSeparatedTextContaining("B"); // FAIL
     */
    public JsonNodeAssert hasSeparatedTextContaining(final String expected) {
        hasTextContaining(expected);
        doesNotContainRegex(".*[^\\s]" + expected + ".*");
        doesNotContainRegex(".*" + expected + "[^\\s].*");
        return this;
    }

    public JsonNodeAssert hasTextNotContaining(final String expected) {
        assertNodeIsOfType(STRING);
        if (actual.asText().contains(expected)) {
            failWithMessage("Expected JsonNode's text to NOT contain <%s> but was <%s>", expected, actual.asText());
        }
        return this;
    }

    public JsonNodeAssert doesNotContainRegex(final String regex) {
        assertNodeIsOfType(STRING);
        if (actual.asText().matches(regex)) {
            failWithMessage("Expected JsonNode's text to NOT contain regex <%s> but was <%s>", regex, actual.asText());
        }
        return this;
    }

    public JsonNodeAssert hasValueEqualTo(int expected) {
        assertNodeIsOfType(NUMBER);
        if (!Objects.equals(actual.intValue(), expected)) {
            failWithMessage("Expected JsonNode's text to be <%s> but was <%s>", expected, actual.intValue());
        }
        return this;
    }

    public JsonNodeAssert isArrayWithNumberOfChildren(int expected) {
        assertNodeIsOfType(ARRAY);
        if (!Objects.equals(actual.size(), expected)) {
            failWithMessage("Expected JsonNode child count to be <%s> but was <%s>", expected, actual.size());
        }
        return this;
    }

    public JsonNodeAssert isArrayWithChildren(String... expected) {
        assertNodeIsOfType(ARRAY);
        VavrAssertions.assertThat(Array.ofAll(actual).map(JsonNode::asText)).containsExactly(expected);
        return this;
    }

    public JsonNodeAssert isEmpty() {
        if (!Objects.equals(actual.isEmpty(), true)) {
            failWithMessage("Expected JsonNode to be empty but was not");
        }
        return this;
    }

    public JsonNodeAssert isObject(Map<String, String> expected) {
        assertNodeIsOfType(OBJECT);

        expected.forEach((key, value) -> assertThat(actual.path(key)).hasTextEqualTo(value));

        return this;
    }

    private void assertNodeIsOfType(JsonNodeType type) {
        if (!Objects.equals(actual.getNodeType(), type)) {
            failWithMessage("Expected JsonNode to be of type <%s> but was <%s>", type, actual.getNodeType());
        }
    }
}
