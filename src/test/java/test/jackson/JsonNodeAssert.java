package test.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

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
        if (!Objects.equals(actual.getNodeType(), STRING)) {
            failWithMessage("Expected JsonNode's to be of type <%s> but was <%s>", STRING, actual.getNodeType());
        }
        if (!Objects.equals(actual.asText(), expected)) {
            failWithMessage("Expected JsonNode's text to be <%s> but was <%s>", expected, actual.asText());
        }
        return this;
    }
}
