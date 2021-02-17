package test.postinstall;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Quantity;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;

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

    static String getRemainingNodeCapacityDescription(final Traversable<Node> nodes) {
        return nodes.toJavaMap(Utils::getNodeResourceSummary)
                .toString();
    }

    private static Tuple2<String, String> getNodeResourceSummary(Node node) {
        return Tuple.of(
                node.getMetadata().getName(),
                getQuantitiesDescription(node.getStatus().getAllocatable()));
    }
}
