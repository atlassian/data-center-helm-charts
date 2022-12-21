package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.control.Option;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static test.jackson.JsonNodeAssert.assertThat;

public class Env {
    private final JsonNode node;

    public Env(JsonNode node) {
        this.node = node;
    }

    public Env assertHasValue(String envName, String expectedValue) {
        assertThat(findEnv(envName))
                .describedAs("Expected env '%s' to have a value", envName)
                .hasValueSatisfying(node ->
                        assertThat(node.path("value"))
                                .describedAs("Expected env '%s' to have the expected value", envName)
                                .hasTextEqualTo(expectedValue));

        return this;
    }

    public Env assertHasSecretRef(String envName, String expectedSecretName, String expectedSecretKey) {
        assertThat(findEnv(envName))
                .describedAs("Expected env '%s' to have a value", envName)
                .hasValueSatisfying(node ->
                        assertThat(node.path("valueFrom").path("secretKeyRef"))
                                .describedAs("Expected env '%s' to have the expected value", envName)
                                .isObject(Map.of(
                                        "name", expectedSecretName,
                                        "key", expectedSecretKey)));

        return this;
    }

    public Env assertHasConfigMapRef(String envName, String expectedConfigMapName, String expectedConfigMapKey) {
        assertThat(findEnv(envName))
                .describedAs("Expected env '%s' to have a value", envName)
                .hasValueSatisfying(node ->
                        assertThat(node.path("valueFrom").path("configMapKeyRef"))
                                .describedAs("Expected env '%s' to have the expected value", envName)
                                .isObject(Map.of(
                                        "name", expectedConfigMapName,
                                        "key", expectedConfigMapKey)));

        return this;
    }

    public Env assertHasFieldRef(String envName, String expectedFieldPath) {
        assertThat(findEnv(envName))
                .describedAs("Expected env '%s' to have a value", envName)
                .hasValueSatisfying(node ->
                        assertThat(node.path("valueFrom").path("fieldRef"))
                                .describedAs("Expected env '%s' to have the expected value", envName)
                                .isObject(Map.of("fieldPath", expectedFieldPath)));

        return this;
    }

    public Env assertDoesNotHaveAnyOf(String... envNames) {
        assertThat(Array.of(envNames).flatMap(this::findEnv).map(node -> node.path("name").asText()))
                .describedAs("None of the environment variables %s should be present", Arrays.asList(envNames))
                .isEmpty();
        return this;
    }

    private Option<JsonNode> findEnv(String envName) {
        return Array.ofAll(node)
                .find(node -> envName.equals(node.required("name").asText()));
    }
}
