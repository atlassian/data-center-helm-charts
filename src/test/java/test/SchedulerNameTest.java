package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests the "schedulerName" value structure in the Helm charts
 */
class SchedulerNameTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void custom_scheduler(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "schedulerName", "second_scheduler"));

        JsonNode podSpec = resources.getStatefulSet(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("schedulerName")).hasTextEqualTo("second_scheduler");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void default_scheduler(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "schedulerName", ""));

        JsonNode podSpec = resources.getStatefulSet(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("schedulerName")).isEmpty();
    }
}
