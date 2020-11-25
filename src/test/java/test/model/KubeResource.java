package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;

/**
 * Reprents a single Kubernetes resource descriptor.
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

    public String getName() {
        return getMetadata().required("name").asText();
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

    static KubeResource wrap(ObjectNode node) {
        final var kind = Kind.valueOf(node.required("kind").asText());
        switch (kind) {
            case StatefulSet:
                return new StatefulSet(node);
            default:
                return new KubeResource(kind, node);
        }
    }
}
