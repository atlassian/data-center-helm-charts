package test.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A specialisation of {@link KubeResource} which adds convenience methods for making StatefulSets easier to handle.
 */
public final class StatefulSet extends KubeResource {
    StatefulSet(JsonNode node) {
        super(Kind.StatefulSet, node);
    }

    public JsonNode getContainers() {
        return getPodSpec().required("containers");
    }

    public JsonNode getPodSpec() {
        return getNode("spec", "template", "spec");
    }
}
