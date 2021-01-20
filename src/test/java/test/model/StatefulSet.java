package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;

/**
 * A specialisation of {@link KubeResource} which adds convenience methods for making StatefulSets easier to handle.
 */
public final class StatefulSet extends KubeResource {
    StatefulSet(JsonNode node) {
        super(Kind.StatefulSet, node);
    }

    private Seq<Container> getContainers() {
        return Array.ofAll(getPodSpec().required("containers")).map(Container::new);
    }

    public Container getContainer() {
        return getContainers().single();
    }

    public Container getContainer(String name) {
        return getContainers()
                .find(c -> name.equals(c.get("name").textValue()))
                .getOrElseThrow(() ->
                        new AssertionError("No container found with name " + name));
    }

    public JsonNode getPodSpec() {
        return getNode("spec", "template", "spec");
    }

    public Seq<JsonNode> getVolumeClaimTemplates() {
        return Array.ofAll(getSpec().path("volumeClaimTemplates"));
    }
}
