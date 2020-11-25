package test.model;

import com.fasterxml.jackson.databind.JsonNode;

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

}
