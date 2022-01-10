package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class PodAnnotationsTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }
    
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void pod_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podAnnotations.podAnnotation1", "podOfHumpbacks",
                "podAnnotations.podAnnotation2", "podOfOrcas"
        ));
        
        final var annotations = resources.getStatefulSet(product.getHelmReleaseName()).getPodMetadata().get("annotations");
        
        assertThat(annotations).isObject(Map.of(
                "podAnnotation1", "podOfHumpbacks",
                "podAnnotation2", "podOfOrcas"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void bamboo_agent_pod_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podAnnotations.podAnnotation1", "podOfHumpbacks",
                "podAnnotations.podAnnotation2", "podOfOrcas"
        ));

        final var annotations = resources.getDeployment(product.getHelmReleaseName()).getPodMetadata().get("annotations");

        assertThat(annotations).isObject(Map.of(
                "podAnnotation1", "podOfHumpbacks",
                "podAnnotation2", "podOfOrcas"
        ));
    }
}
