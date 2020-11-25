package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests the various permutations of the "image" value structure in the Helm charts
 */
class ImageTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void image_tag(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.tag", "myversion"
        ));

        resources.getStatefulSets()
                .forEach(statefulSet -> assertThat(statefulSet.getContainer().get("image"))
                        .describedAs("StatefulSet %s should have the configured image", statefulSet.getName())
                        .hasTextEqualTo("%s:myversion", product.getDockerImageName()));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void image_registry_and_tag(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.registry", "myregistry.io",
                "image.tag", "myversion"
        ));

        resources.getStatefulSets()
                .forEach(statefulSet -> assertThat(statefulSet.getContainer().get("image"))
                        .describedAs("StatefulSet %s should have the configured image", statefulSet.getName())
                        .hasTextEqualTo("myregistry.io/%s:myversion", product.getDockerImageName()));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void image_registry_repository_tag(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.registry", "myregistry.io",
                "image.repository", "myorg/myimage",
                "image.tag", "myversion"
        ));

        resources.getStatefulSets()
                .forEach(statefulSet -> assertThat(statefulSet.getContainer().get("image"))
                        .describedAs("StatefulSet %s should have the configured image", statefulSet.getName())
                        .hasTextEqualTo("myregistry.io/myorg/myimage:myversion"));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void image_pullPolicy(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.pullPolicy", "Always"));

        resources.getStatefulSets()
                .forEach(statefulSet -> assertThat(statefulSet.getContainer().get("imagePullPolicy"))
                        .describedAs("StatefulSet %s should have the configured imagePullPolicy", statefulSet.getName())
                        .hasTextEqualTo("Always"));
    }
}
