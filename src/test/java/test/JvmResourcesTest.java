package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the "resources.jvm" value structure in the Helm charts
 */
class JvmResourcesTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void changes_annotation_checksum(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".resources.jvm.maxHeap", "768m"
        ));

        final var metadata = resources.getStatefulSet(product.getHelmReleaseName()).getPodMetadata();
        final var checksum = metadata.get("annotations").get("checksum/config-jvm");

        assertThat(checksum).isNotNull();

        final var resourcesWithChanges = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".resources.jvm.maxHeap", "1024m"
        ));

        final var metadataWithChanges = resourcesWithChanges.getStatefulSet(product.getHelmReleaseName()).getPodMetadata();
        final var checksumWithChanges = metadataWithChanges.get("annotations").get("checksum/config-jvm");

        assertThat(checksumWithChanges)
                .isNotNull()
                .isNotEqualTo(checksum);
    }
}
