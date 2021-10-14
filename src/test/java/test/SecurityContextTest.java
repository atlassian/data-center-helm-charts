package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests the various permutations of the "<product>.securityContext" and "<product>.containerSecurityContext" value structure in the Helm charts
 */
class SecurityContextTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pod_security_context(Product product) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".securityContext.fsGroup", "1000",
                product + ".securityContext.runAsGroup", "1000"));

        JsonNode podSpec = resources.getStatefulSet(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("securityContext").path("fsGroup")).hasValueEqualTo(1000);
        assertThat(podSpec.path("securityContext").path("runAsGroup")).hasValueEqualTo(1000);

    }

    @ParameterizedTest
    @CsvSource({
            "jira,2001",
            "confluence,2002",
            "bitbucket,2003",
            "crowd,2004",
            "bamboo,2005"
    })
    void test_pod_security_context_without_fsGroup(Product product, int fsGroup) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".securityContext.runAsGroup", "1000"));

        JsonNode podSpec = resources.getStatefulSet(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("securityContext").path("fsGroup")).hasValueEqualTo(fsGroup);
        assertThat(podSpec.path("securityContext").path("runAsGroup")).hasValueEqualTo(1000);

    }

    @ParameterizedTest
    @CsvSource({
            "jira,2001",
            "confluence,2002",
            "bitbucket,2003",
            "crowd,2004" // Bamboo didn't have 1.0.0 release that needs to be backward compatible
    })
    void test_pod_security_context_backward_compatible(Product product) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".securityContext.fsGroup", "null",
                product + ".securityContext.gid", "1000",
                product + ".securityContext.enabled", "true"));

        JsonNode podSpec = resources.getStatefulSet(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("securityContext").path("fsGroup")).hasValueEqualTo(1000);
    }

    @ParameterizedTest
    @CsvSource({
            "jira,2001",
            "confluence,2002",
            "bitbucket,2003",
            "crowd,2004"
    })
    void test_pod_security_context_backward_compatible_disabled_context(Product product, int fsGroup) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".securityContext.fsGroup", "null",
                product + ".securityContext.gid", "1000",
                product + ".securityContext.enabled", "false"));

        JsonNode podSpec = resources.getStatefulSet(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("securityContext").path("fsGroup")).hasValueEqualTo(fsGroup);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_container_security_context(Product product) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".containerSecurityContext.runAsGroup", "2000"));

        JsonNode containerSecurityContext = resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getSecurityContext();
        assertThat(containerSecurityContext.path("runAsGroup")).hasValueEqualTo(2000);
    }
}
