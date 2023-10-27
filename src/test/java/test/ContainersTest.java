package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.PersistentVolumeClaim;

/**
 * Tests the various permutations of the "persistence" value structure in the Helm charts
 */
class ContainersTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additionalEnvironmentVariables(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname+".additionalEnvironmentVariables[0].name", "MY_ENV_VAR",
                pname+".additionalEnvironmentVariables[0].value", "env-value"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("MY_ENV_VAR", "env-value");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additionalPorts(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname+".additionalPorts[0].name", "jmx",
                pname+".additionalPorts[0].port", "5555",
                pname+".additionalPorts[0].protocol", "TCP"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var port = statefulSet.getContainer().getPort("jmx");
        assertThat(port.path("name")).hasTextEqualTo("jmx");
        assertThat(port.path("protocol")).hasTextEqualTo("TCP");
        assertThat(port.path("port")).hasValueEqualTo(5555);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additionalInitContainers(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalInitContainers[0].name", "my_init_container",
                "additionalInitContainers[0].image", "my_init_image"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var icontainer = statefulSet.getInitContainer("my_init_container").get();
        assertThat(icontainer.path("image")).hasTextEqualTo("my_init_image");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additionalContainers(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalContainers[0].name", "my_container",
                "additionalContainers[0].image", "my_image"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var icontainer = statefulSet.getContainer("my_container");
        assertThat(icontainer.get("image")).hasTextEqualTo("my_image");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void defaultContainerNames(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var containerName = statefulSet.getContainer().get("name");
        assertThat(containerName).hasTextEqualTo(product.name());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void containerNamesAsHelmReleaseName(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".useHelmReleaseNameAsContainerName", "true"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var containerName = statefulSet.getContainer().get("name");
        assertThat(containerName).hasTextEqualTo(product.getHelmReleaseName());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void nfx_permission_fixer_container_default_no_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
        ));

        final var nfsPermissionFixerContainer = resources.getStatefulSet(product.getHelmReleaseName()).getInitContainers().get(0);
        assertThat(nfsPermissionFixerContainer.get("resources")).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void nfx_permission_fixer_container_default_custom_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                "volumes.sharedHome.nfsPermissionFixer.resources.requests.cpu", "1",
                "volumes.sharedHome.nfsPermissionFixer.resources.requests.memory", "2Gi",
                "volumes.sharedHome.nfsPermissionFixer.resources.limits.cpu", "2",
                "volumes.sharedHome.nfsPermissionFixer.resources.limits.memory", "3Gi"
        ));

        final var nfsPermissionFixerContainer = resources.getStatefulSet(product.getHelmReleaseName()).getInitContainers().get(0);
        assertThat(nfsPermissionFixerContainer.get("resources").get("requests").get("cpu")).hasValueEqualTo(1);
        assertThat(nfsPermissionFixerContainer.get("resources").get("requests").get("memory")).hasTextEqualTo("2Gi");
        assertThat(nfsPermissionFixerContainer.get("resources").get("limits").get("cpu")).hasValueEqualTo(2);
        assertThat(nfsPermissionFixerContainer.get("resources").get("limits").get("memory")).hasTextEqualTo("3Gi");
    }
}
