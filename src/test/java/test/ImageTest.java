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
                .filter(s -> product.getHelmReleaseName().equals(s.getName()))
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
                .filter(s -> product.getHelmReleaseName().equals(s.getName()))
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
                .filter(s -> product.getHelmReleaseName().equals(s.getName()))
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

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "bitbucket", "confluence", "bamboo"})
    void fluentd_custom_image_test(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.imageRepo", "registry.com",
                "fluentd.imageTag", "mytag"
        ));

        final var fluentContainerImage = resources.getStatefulSet(product.getHelmReleaseName()).getContainer("fluentd").get("image");

        assertThat(fluentContainerImage).hasTextEqualTo("registry.com:mytag");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "bitbucket", "confluence", "bamboo"})
    void fluentd_default_image_test(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true"
        ));

        final var fluentContainerImage = resources.getStatefulSet(product.getHelmReleaseName()).getContainer("fluentd").get("image");

        assertThat(fluentContainerImage).hasTextContaining("fluent/fluentd");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "bitbucket", "confluence", "bamboo"})
    void nfs_permission_fixer_custom_image_test(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                "volumes.sharedHome.nfsPermissionFixer.imageRepo", "registry.com",
                "volumes.sharedHome.nfsPermissionFixer.imageTag", "mytag"
        ));

        final var permissionFixerImage = resources.getStatefulSet(product.getHelmReleaseName()).getInitContainer("nfs-permission-fixer").get().path("image");

        assertThat(permissionFixerImage).hasTextEqualTo("registry.com:mytag");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "bitbucket", "confluence", "bamboo"})
    void nfs_permission_fixer_default_image_test(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
                ));
        final var permissionFixerImage = resources.getStatefulSet(product.getHelmReleaseName()).getInitContainer("nfs-permission-fixer").get().path("image");

        assertThat(permissionFixerImage).hasTextContaining("alpine");
    }
}
