package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;
import java.util.Map;
import static test.jackson.JsonNodeAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadinessLivenessProbesTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_readiness_probe_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".readinessProbe.enabled", "false"));

        assertThat(resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_liveness_probe_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".livenessProbe.enabled", "false"));

        assertThat(resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe")).isEmpty();
        assertThat(resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_liveness_probe_defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        assertEquals("5", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("periodSeconds").asText());
        assertEquals("1", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("failureThreshold").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_liveness_probe_overrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".livenessProbe.periodSeconds", "10",
                product + ".livenessProbe.failureThreshold", "10"));

        assertEquals("10", resources.getStatefulSet(
                        product.getHelmReleaseName()).getContainer().get("livenessProbe").get("periodSeconds").asText());
        assertEquals("10", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("failureThreshold").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_readiness_probe_overrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".readinessProbe.initialDelaySeconds", "10",
                product + ".readinessProbe.periodSeconds", "10",
                product + ".readinessProbe.failureThreshold", "10"));

        assertEquals("10", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("initialDelaySeconds").asText());
        assertEquals("10", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("periodSeconds").asText());
        assertEquals("10", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("failureThreshold").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_readiness_probe_custom_probe(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".readinessProbe.customProbe.tcpSocket.port", "9999",
                product + ".readinessProbe.customProbe.periodSeconds", "10",
                product + ".readinessProbe.customProbe.failureThreshold", "10",
                product + ".readinessProbe.customProbe.foo", "bar"));

        assertEquals("9999", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("tcpSocket").get("port").asText());
        assertEquals("10", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("periodSeconds").asText());
        assertEquals("10", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("failureThreshold").asText());
        assertEquals("bar", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("foo").asText());
    }
}
