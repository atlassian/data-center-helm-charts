package test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests the various permutations of the "<product>.contextPath" value structure in the Helm charts
 */
class ContextPathTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "jira"})
    void test_context_path(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.contextPath", "/" + product.name()));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_TOMCAT_CONTEXTPATH", "/" + product.name());
        
            assertEquals(resources.getStatefulSet(
                    product.getHelmReleaseName()).getContainer().get("readinessProbe").get("httpGet").get("path").asText(),
                    "/" + product.name() + "/status");
    }
    
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo"})
    void test_context_path_bamboo(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.contextPath", "/" + product.name()));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_TOMCAT_CONTEXTPATH", "/" + product.name());

            assertEquals(resources.getStatefulSet(
                    product.getHelmReleaseName()).getContainer().get("readinessProbe").get("httpGet").get("path").asText(),
                    "/" + product.name() + "/rest/api/latest/status");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"})
    void test_context_path_bitbucket(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.contextPath", "/" + product.name()));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("SERVER_CONTEXT_PATH", "/" + product.name());

        assertEquals(resources.getStatefulSet(
                        product.getHelmReleaseName()).getContainer().get("readinessProbe").get("httpGet").get("path").asText(),
                "/" + product.name() + "/status");
    }
}
