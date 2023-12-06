package test.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.collection.Traversable;

public class Pod extends KubeResource {
    Pod(JsonNode node) {
        super(Kind.Pod, node);
    }

    public JsonNode getPodMetadata() {
        return getNode("metadata");
    }

}
