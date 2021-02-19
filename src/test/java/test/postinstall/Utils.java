package test.postinstall;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.control.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static io.fabric8.kubernetes.api.model.Quantity.getAmountInBytes;
import static java.nio.file.Files.newBufferedReader;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

final class Utils {
    static Map<String, String> readPropertiesFile(final Path fileLocation) throws IOException {
        final var properties = new Properties();
        try (var reader = newBufferedReader(fileLocation)) {
            properties.load(reader);
        }
        return HashMap.ofAll(properties)
                .mapKeys(String.class::cast)
                .mapValues(String.class::cast);
    }

    static String getQuantitiesDescription(final java.util.Map<String, Quantity> allocatable) {
        return Array.empty()
                .appendAll(HashMap.ofAll(allocatable)
                        .get("memory")
                        .map(Utils::getDataSize)
                        .map(dataSize -> String.format("memory=%s", dataSize)))
                .appendAll(HashMap.ofAll(allocatable)
                        .get("cpu")
                        .map(quantity -> String.format("cpu=%s", quantity)))
                .mkString(", ");
    }

    private static String getDataSize(Quantity quantity) {
        return byteCountToDisplaySize(getAmountInBytes(quantity).toBigInteger());
    }

    static String netNodesResourceSummary(final Map<Node, Option<NodeMetrics>> nodes) {
        return nodes.map(Utils::getNodeResourceSummary)
                .toJavaMap()
                .toString();
    }

    private static Tuple2<String, String> getNodeResourceSummary(Node node, Option<NodeMetrics> metrics) {
        final var allocatableDescription = getQuantitiesDescription(node.getStatus().getAllocatable());
        final var usageDescription = metrics.map(m -> getQuantitiesDescription(m.getUsage()))
                .getOrElse("metrics unavailable");

        final var description = String.format("usage=[%s], capacity=[%s]", usageDescription, allocatableDescription);

        return Tuple.of(node.getMetadata().getName(), description);
    }
}
