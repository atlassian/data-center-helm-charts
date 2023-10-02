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
                "podAnnotations.podAnnotation2", "podOfOrcas",
                "podAnnotations.podAnnotation3", "'{{ \"podOfTucuxis\" | b64enc }}'",
                "podAnnotations.podAnnotation4", "'{{ \"podOfTucuxis\" | upper }}'"

        ));

        final var annotations = resources.getStatefulSet(product.getHelmReleaseName()).getPodMetadata().get("annotations");

        assertThat(annotations).isObject(Map.of(
                "podAnnotation1", "podOfHumpbacks",
                "podAnnotation2", "podOfOrcas",
                "podAnnotation3", "'" + b64enc("podOfTucuxis") + "'",
                "podAnnotation4", "'PODOFTUCUXIS'"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void bamboo_agent_pod_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podAnnotations.podAnnotation1", "podOfHumpbacks",
                "podAnnotations.podAnnotation2", "podOfOrcas",
                "podAnnotations.podAnnotation3", "'{{ \"podOfTucuxis\" | b64enc }}'",
                "podAnnotations.podAnnotation4", "'{{ \"podOfTucuxis\" | upper }}'"
        ));

        final var annotations = resources.getDeployment(product.getHelmReleaseName()).getPodMetadata().get("annotations");

        assertThat(annotations).isObject(Map.of(
                "podAnnotation1", "podOfHumpbacks",
                "podAnnotation2", "podOfOrcas",
                "podAnnotation3", "'" + b64enc("podOfTucuxis") + "'",
                "podAnnotation4", "'PODOFTUCUXIS'"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_pod_default_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podAnnotations.confluence", "annotation",
                "podAnnotations.confluence1", "annotation1",
                "synchrony.enabled", "true"
        ));

        final var annotations = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony").getPodMetadata().get("annotations");

        assertThat(annotations).isObject(Map.of(
                "confluence", "annotation",
                "confluence1", "annotation1"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_pod_custom_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.podAnnotations.synchrony", "annotation",
                "synchrony.podAnnotations.synchrony1", "annotation1",
                "synchrony.enabled", "true"
        ));

        final var annotations = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony").getPodMetadata().get("annotations");

        assertThat(annotations).isObject(Map.of(
                "synchrony", "annotation",
                "synchrony1", "annotation1"

        ));
    }

    private static String b64enc(String string) {
        return new String(java.util.Base64.getEncoder().encode(string.getBytes()));
    }
}
