package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Traversable;

/**
 * A specialisation of {@link KubeResource} which adds convenience methods for making StatefulSets easier to handle.
 */
public final class StatefulSet extends KubeResource {
    StatefulSet(JsonNode node) {
        super(Kind.StatefulSet, node);
    }

    public Traversable<JsonNode> getContainers() {
        return Array.ofAll(getPodSpec().required("containers"));
    }

    public JsonNode getPodSpec() {
        return getNode("spec", "template", "spec");
    }
}
