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
    @EnumSource(value = Product.class, names = {"confluence", "jira", "bamboo"})
    void test_context_path(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.contextPath", "/" + product.name()));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_TOMCAT_CONTEXTPATH", "/" + product.name());


        if(product.name().equals("bamboo")) {
            assertEquals(resources.getStatefulSet(
                    product.getHelmReleaseName()).getContainer().get("readinessProbe").get("httpGet").get("path").asText(),
                    "/" + product.name() + "/rest/api/latest/status");
        } else {
            assertEquals(resources.getStatefulSet(
                    product.getHelmReleaseName()).getContainer().get("readinessProbe").get("httpGet").get("path").asText(),
                    "/" + product.name() + "/status");
        }
    }
}
