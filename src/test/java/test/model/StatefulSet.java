package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;

/**
 * A specialisation of {@link KubeResource} which adds convenience methods for making StatefulSets easier to handle.
 */
public final class StatefulSet extends KubeResource {
    StatefulSet(JsonNode node) {
        super(Kind.StatefulSet, node);
    }

    private Traversable<Container> getContainers() {
        return Array.ofAll(getPodSpec().required("containers")).map(Container::new);
    }

    public Container getContainer() {
        return getContainers().single();
    }

    public JsonNode getPodSpec() {
        return getNode("spec", "template", "spec");
    }

    public Seq<JsonNode> getVolumeClaimTemplates() {
        return Array.ofAll(getSpec().path("volumeClaimTemplates"));
    }
}
