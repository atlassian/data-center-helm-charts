package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

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

    public int getReplicas() {
        return getSpec().get("replicas").intValue();
    }

    public JsonNode getPodSpec() {
        return getNode("spec", "template", "spec");
    }

    public JsonNode getPodMetadata() {
        return getNode("spec", "template", "metadata");
    }

    public Seq<JsonNode> getVolumeClaimTemplates() {
        return Array.ofAll(getSpec().path("volumeClaimTemplates"));
    }

    public JsonNode getVolumes() {
        return getPodSpec().required("volumes");
    }

    public JsonNode getNodeSelector() { return getNode("spec", "template", "spec", "nodeSelector"); }

    public Option<JsonNode> getVolume(String volumeName) {
        return Array.ofAll(getVolumes())
                .find(volume -> volume.path("name").asText().equals(volumeName));
    }

    public JsonNode getInitContainers() {
        return getPodSpec().required("initContainers");
    }

    public Option<JsonNode> getInitContainer(String name) {
        return Array.ofAll(getInitContainers())
                .find(volume -> volume.path("name").asText().equals(name));
    }

    public JsonNode getLabels() {
        return getPodMetadata().path("labels");
    }
}
