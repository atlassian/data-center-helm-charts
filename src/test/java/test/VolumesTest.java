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
class VolumesTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource
    void localHome_pvc_create(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.localHome.persistentVolumeClaim.create", "true"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        assertThat(statefulSet.getVolumeClaimTemplates())
                .describedAs("StatefulSet %s should have a single volumeClaimTemplate", statefulSet.getName())
                .hasSize(1);

        verifyVolumeClaimTemplate(
                statefulSet.getVolumeClaimTemplates().head(),
                "local-home", "ReadWriteOnce");

        assertThat(statefulSet.getVolume("local-home"))
                .describedAs("StatefulSet %s should not have a local-home volume in the pod spec", statefulSet.getName())
                .isEmpty();
    }

    @ParameterizedTest
    @EnumSource
    void sharedHome_pvc_create(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
        ));

        final var kubeResource = resources.get(PersistentVolumeClaim);
        Assertions.assertThat(kubeResource.getName()).isEqualTo(product.getHelmReleaseName() + "-shared-home");

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        assertThat(statefulSet.getVolume("shared-home"))
                .describedAs("StatefulSet %s should not have a local-home volume in the pod spec", statefulSet.getName())
                .hasValueSatisfying(volume ->
                        assertThat(volume.required("persistentVolumeClaim").required("claimName"))
                                .hasTextEqualTo(product.getHelmReleaseName() + "-shared-home"));
    }

    @ParameterizedTest
    @EnumSource
    void localHome_custom_volume(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.localHome.customVolume.hostPath", "/foo/bar" // not actually a valid hostPath definition, but it works for the test
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        assertThat(statefulSet.getVolume("local-home"))
                .hasValueSatisfying(localHomeVolume -> assertThat(localHomeVolume).isObject(Map.of("hostPath", "/foo/bar")));
    }

    @ParameterizedTest
    @EnumSource
    void localHome_pvc_custom(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.localHome.persistentVolumeClaim.create", "true",
                "volumes.localHome.persistentVolumeClaim.storageClassName", "foo",
                "volumes.localHome.persistentVolumeClaim.resources.requests.storage", "2Gi"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        final var localHomeVolumeClaimTemplate = statefulSet.getVolumeClaimTemplates().head();
        verifyVolumeClaimTemplate(localHomeVolumeClaimTemplate, "local-home", "ReadWriteOnce");
        assertThat(localHomeVolumeClaimTemplate.path("spec").path("storageClassName"))
                .hasTextEqualTo("foo");
        assertThat(localHomeVolumeClaimTemplate.path("spec").path("resources").path("requests").path("storage"))
                .hasTextEqualTo("2Gi");
    }

    @ParameterizedTest
    @EnumSource
    void sharedHome_custom_volume(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.customVolume.hostPath", "/foo/bar" // not actually a valid hostPath definition, but it works for the test
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        assertThat(statefulSet.getVolume("shared-home"))
                .hasValueSatisfying(localHomeVolume -> assertThat(localHomeVolume).isObject(Map.of("hostPath", "/foo/bar")));
    }

    @ParameterizedTest
    @EnumSource
    void additionalVolumeDefinition(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.additional[0].name", "my-volume",
                "volumes.additional[0].persistentVolumeClaim.claimName", "my-volume-pvc"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getVolume("my-volume").get().path("persistentVolumeClaim").path("claimName"))
                .hasTextEqualTo("my-volume-pvc");
    }

    @ParameterizedTest
    @EnumSource
    void additionalVolumeMounts(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname+".additionalVolumeMounts[0].name", "my-volume-mount",
                pname+".additionalVolumeMounts[0].mountPath", "/my-volume-path",
                pname+".additionalVolumeMounts[0].subPath", "extra_path"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var mount = statefulSet.getContainer().getVolumeMount("my-volume-mount");
        assertThat(mount.get("mountPath")).hasTextEqualTo("/my-volume-path");
        assertThat(mount.get("subPath")).hasTextEqualTo("extra_path");
    }

    @ParameterizedTest
    @EnumSource
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

    private void verifyVolumeClaimTemplate(JsonNode volumeClaimTemplate, final String expectedVolumeName, final String... expectedAccessModes) {
        assertThat(volumeClaimTemplate.path("metadata").path("name"))
                .hasTextEqualTo(expectedVolumeName);
        assertThat(volumeClaimTemplate.path("spec").path("accessModes"))
                .isArrayWithChildren(expectedAccessModes);
    }
}
