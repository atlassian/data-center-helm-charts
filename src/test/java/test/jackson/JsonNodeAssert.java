package test.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.vavr.collection.Array;
import org.assertj.core.api.AbstractAssert;
import org.assertj.vavr.api.VavrAssertions;

import java.util.Objects;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
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

    private void assertNodeIsOfType(JsonNodeType type) {
        if (!Objects.equals(actual.getNodeType(), type)) {
            failWithMessage("Expected JsonNode to be of type <%s> but was <%s>", type, actual.getNodeType());
        }
    }
}
