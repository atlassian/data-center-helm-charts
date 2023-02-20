package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;

/**
 * Represents a single Kubernetes resource descriptor.
 */
public class KubeResource {
    private final Kind kind;
    private final JsonNode node;

    KubeResource(Kind kind, JsonNode node) {
        this.kind = kind;
        this.node = node;
    }

    public Kind getKind() {
        return kind;
    }

    public JsonNode getMetadata() {
        return node.required("metadata");
    }

    public JsonNode getSpec() {
        return node.required("spec");
    }
    public JsonNode getConfigMapData() {
        return node.required("data");
    }
    public String getName() {
        return getMetadata().required("name").asText();
    }

    public JsonNode getAnnotations() {
        return getMetadata().required("annotations");
    }

    public JsonNode getNode(String... paths) {
        return get(node, Array.of(paths));
    }

    private static JsonNode get(JsonNode node, Seq<String> paths) {
        if (paths.isEmpty()) {
            return node;
        } else {
            return get(node.required(paths.head()), paths.tail());
        }
    }

    static KubeResource wrap(JsonNode node) {
        final var kind = Kind.valueOf(node.required("kind").asText());
        switch (kind) {
            case StatefulSet:
                return new StatefulSet(node);
            case Service:
                return new Service(node);
            case Deployment:
                return new Deployment(node);
            case ConfigMap:
                return new ConfigMap(node);
            default:
                return new KubeResource(kind, node);
        }
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getKind(), getName());
    }
}
