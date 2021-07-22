package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;

public final class Container {
    private final JsonNode node;

    public Container(JsonNode node) {
        this.node = node;
    }

    public JsonNode get(String fieldName) {
        return node.path(fieldName);
    }

    public Env getEnv() {
        return new Env(node.path("env"));
    }

    public JsonNode getVolumeMounts() {
        return node.path("volumeMounts");
    }

    public JsonNode getVolumeMount(String name) {
        return Array.ofAll(getVolumeMounts())
                .find(v -> v.path("name").asText().equals(name))
                .getOrElseThrow(() -> new AssertionError("cannot find the volume mount: " + name));

    }

    public JsonNode getResources() {
        return get("resources");
    }

    public JsonNode getRequests() {
        return getResources().path("requests");
    }

    public JsonNode getLimits() {
        return getResources().path("limits");
    }
}
