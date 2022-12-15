package test.model;

import com.fasterxml.jackson.databind.JsonNode;

public final class ConfigMap extends KubeResource {

    ConfigMap(JsonNode node) {
        super(Kind.ConfigMap, node);
    }

    public JsonNode getDataByKey(String key) {
        return getConfigMapData().path(key);
    }
}
