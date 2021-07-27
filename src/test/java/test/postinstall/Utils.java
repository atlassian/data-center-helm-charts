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
import test.model.Product;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

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

    private static AtomicReference<Map<String, String>> helmParameters = new AtomicReference<>();
    static void loadHelmParameters() throws IOException {
        final var helmParametersFileLocation = System.getProperty("helmParametersFileLocation");
        if (helmParametersFileLocation != null) {
            final var fileParameters = Utils.readPropertiesFile(Path.of(helmParametersFileLocation));

            var productName = fileParameters.get("PRODUCT_NAME").get();
            var prefix = fileParameters.get("RELEASE_PREFIX").get();
            var ns = fileParameters.get("TARGET_NAMESPACE").get();
            var helmReleaseName = Array.of(prefix, productName).mkString("-");

            var params = HashMap.of(
                    "product", productName,
                    "prefix", prefix,
                    "release", helmReleaseName,
                    "ns", ns
            );
            helmParameters.set(params);

        } else {
            var params = HashMap.of(
                    "product", System.getProperty("helmProduct"),
                    "prefix", System.getProperty("helmProduct"),
                    "release", System.getProperty("helmRelease"),
                    "ns", System.getProperty("namespace")
            );
            helmParameters.set(params);
        }
    }

    static String getNS() {
        return helmParameters.get().get("ns").get();
    }

    static String getRelease() {
        return helmParameters.get().get("release").get();
    }

    static Product getProduct() {
        var str = helmParameters.get().get("product").get();
        return Product.valueOf(str);
    }

    static boolean productIs(Product product) {
        return product == getProduct();
    }

}
