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
 * Tests the various permutations of the "<product>.license" value structure in the Helm charts
 */
class ContextPathTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_context_path(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "confluence.service.contextPath", "/confluence"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_TOMCAT_CONTEXTPATH", "/confluence");


        assertEquals(resources.getStatefulSet(
                product.getHelmReleaseName()).getContainer().get("readinessProbe").get("httpGet").get("path").asText(),
                "/confluence/status");
    }
}
