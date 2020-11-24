package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void image(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.registry", "myregistry.io",
                "image.repository", "myorg/myimage",
                "image.tag", "myversion"
        ));

        resources.getStatefulSets()
                .forEach(statefulSet ->
                        assertThat(statefulSet.getContainers()
                                .path(0)
                                .path("image")
                                .asText())
                                .describedAs("StatefulSet %s should have the configured image", statefulSet.getName())
                                .isEqualTo("myregistry.io/myorg/myimage:myversion"));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void image_pullPolicy(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.pullPolicy", "Always"));

        resources.getStatefulSets()
                .forEach(statefulSet ->
                        assertThat(statefulSet.getContainers()
                                .path(0)
                                .path("imagePullPolicy")
                                .asText())
                                .describedAs("StatefulSet %s should have the configured imagePullPolicy", statefulSet.getName())
                                .isEqualTo("Always"));
    }
}
