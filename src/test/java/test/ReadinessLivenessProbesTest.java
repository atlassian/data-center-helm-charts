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
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        assertThat(resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_liveness_probe_overrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".livenessProbe.enabled", "true",
                product + ".livenessProbe.initialDelaySeconds", "1111",
                product + ".livenessProbe.periodSeconds", "2222",
                product + ".livenessProbe.failureThreshold", "3333",
                product + ".livenessProbe.timeoutSeconds", "4444"));

        assertEquals("1111", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("initialDelaySeconds").asText());
        assertEquals("2222", resources.getStatefulSet(
                        product.getHelmReleaseName()).getContainer().get("livenessProbe").get("periodSeconds").asText());
        assertEquals("3333", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("failureThreshold").asText());
        assertEquals("4444", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("timeoutSeconds").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_liveness_probe_enabled_defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".livenessProbe.enabled", "true"));

        assertEquals("60", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("initialDelaySeconds").asText());
        assertEquals("5", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("periodSeconds").asText());
        assertEquals("12", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("failureThreshold").asText());
        assertEquals("1", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("livenessProbe").get("timeoutSeconds").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_readiness_probe_overrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".readinessProbe.initialDelaySeconds", "2222",
                product + ".readinessProbe.periodSeconds", "2222",
                product + ".readinessProbe.failureThreshold", "2222",
                product + ".readinessProbe.timeoutSeconds", "3333"));

        assertEquals("2222", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("initialDelaySeconds").asText());
        assertEquals("2222", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("periodSeconds").asText());
        assertEquals("2222", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("failureThreshold").asText());
        assertEquals("3333", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("timeoutSeconds").asText());

    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_readiness_probe_custom_probe(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".readinessProbe.customProbe.tcpSocket.port", "9999",
                product + ".readinessProbe.customProbe.periodSeconds", "3333",
                product + ".readinessProbe.customProbe.failureThreshold", "3333",
                product + ".readinessProbe.customProbe.timeoutSeconds", "4444",
                product + ".readinessProbe.customProbe.foo", "bar"));

        assertEquals("9999", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("tcpSocket").get("port").asText());
        assertEquals("3333", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("periodSeconds").asText());
        assertEquals("3333", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("failureThreshold").asText());
        assertEquals("4444", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("timeoutSeconds").asText());
        assertEquals("bar", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("foo").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_startup_probe_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".startupProbe.enabled", "true"));

        assertThat(resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe")).isNotNull();

        assertEquals("120", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe").get("failureThreshold").asText());
        assertEquals("60", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe").get("initialDelaySeconds").asText());
        assertEquals("5", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe").get("periodSeconds").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_startup_probe_defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        assertThat(resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_startup_probe_customized(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".startupProbe.enabled", "true",
                product + ".startupProbe.failureThreshold", "1200",
                product + ".startupProbe.initialDelaySeconds", "1200",
                product + ".startupProbe.periodSeconds", "14"));

        assertEquals("1200", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe").get("failureThreshold").asText());
        assertEquals("1200", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe").get("initialDelaySeconds").asText());
        assertEquals("14", resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("startupProbe").get("periodSeconds").asText());
    }
}
