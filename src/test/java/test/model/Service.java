package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;

public class Service extends KubeResource {
    Service(JsonNode node) {
        super(Kind.Service, node);
    }

    public JsonNode getType() {
        return getSpec().path("type");
    }

    public Traversable<JsonNode> getPorts() {
        return Array.ofAll(getSpec().path("ports"));
    }

    public Option<JsonNode> getPort(String name) {
        return getPorts().find(portNode -> name.equals(portNode.path("name").asText()));
    }

    public JsonNode getLoadBalancerIP() {
        return getSpec().path("loadBalancerIP");
    }
}
