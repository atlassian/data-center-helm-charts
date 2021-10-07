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
import static test.model.Kind.PersistentVolume;
import static test.model.Synchrony.synchronyStatefulSetName;

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
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
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
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
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
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void localHome_custom_volume(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.localHome.customVolume.hostPath", "/foo/bar" // not actually a valid hostPath definition, but it works for the test
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        assertThat(statefulSet.getVolume("local-home"))
                .hasValueSatisfying(localHomeVolume -> assertThat(localHomeVolume).isObject(Map.of("hostPath", "/foo/bar")));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void localHome_pvc_custom(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.localHome.persistentVolumeClaim.create", "true",
                "volumes.localHome.persistentVolumeClaim.storageClassName", "foo",
                "volumes.localHome.persistentVolumeClaim.resources.requests.storage", "2Gi",
                "volumes.localHome.mountPath", "/foo/bar"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        final var localHomeVolumeClaimTemplate = statefulSet.getVolumeClaimTemplates().head();
        verifyVolumeClaimTemplate(localHomeVolumeClaimTemplate, "local-home", "ReadWriteOnce");
        assertThat(localHomeVolumeClaimTemplate.path("spec").path("storageClassName"))
                .hasTextEqualTo("foo");
        assertThat(localHomeVolumeClaimTemplate.path("spec").path("resources").path("requests").path("storage"))
                .hasTextEqualTo("2Gi");
        final var mount = statefulSet.getContainer().getVolumeMount("local-home");
        assertThat(mount.get("name")).hasTextEqualTo("local-home");
        assertThat(mount.get("mountPath")).hasTextEqualTo("/foo/bar");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void sharedHome_custom_volume(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.customVolume.hostPath", "/foo/bar" // not actually a valid hostPath definition, but it works for the test
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        assertThat(statefulSet.getVolume("shared-home"))
                .hasValueSatisfying(localHomeVolume -> assertThat(localHomeVolume).isObject(Map.of("hostPath", "/foo/bar")));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
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
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additionalVolumeMounts(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname + ".additionalVolumeMounts[0].name", "my-volume-mount",
                pname + ".additionalVolumeMounts[0].mountPath", "/my-volume-path",
                pname + ".additionalVolumeMounts[0].subPath", "extra_path"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var mount = statefulSet.getContainer().getVolumeMount("my-volume-mount");
        assertThat(mount.get("mountPath")).hasTextEqualTo("/my-volume-path");
        assertThat(mount.get("subPath")).hasTextEqualTo("extra_path");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucketSharedHomeClaimUsesDefaultVolumeName(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolume.create", "true",
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
        ));

        final String volumeName = product.getHelmReleaseName() + "-shared-home-pv";
        final var pvc = resources.get(PersistentVolumeClaim);
        final var pv = resources.get(PersistentVolume);
        Assertions.assertThat(pv.getName()).isEqualTo(volumeName);
        Assertions.assertThat(pvc.getNode("spec").get("volumeName").asText()).isEqualTo(volumeName);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucketSharedHomeClaimUsesSuppliedVolumeName(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolume.create", "false",
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                "volumes.sharedHome.persistentVolumeClaim.volumeName", "my-custom-volume"
        ));
        final var pvc = resources.get(PersistentVolumeClaim);
        Assertions.assertThat(pvc.getNode("spec").get("volumeName").asText()).isEqualTo("my-custom-volume");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchronyHome_pvc_create(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "volumes.synchronyHome.persistentVolumeClaim.create", "true"
        ));

        final var statefulSet = resources.getStatefulSet(synchronyStatefulSetName());

        assertThat(statefulSet.getVolumeClaimTemplates())
                .describedAs("StatefulSet %s should have a single volumeClaimTemplate", statefulSet.getName())
                .hasSize(1);

        verifyVolumeClaimTemplate(
                statefulSet.getVolumeClaimTemplates().head(),
                "synchrony-home", "ReadWriteOnce");

        assertThat(statefulSet.getVolume("synchrony-home"))
                .describedAs("StatefulSet %s should not have a synchrony-home volume in the pod spec", statefulSet.getName())
                .isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchronyHome_custom_volume(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "volumes.synchronyHome.customVolume.hostPath", "/foo/bar" // not actually a valid hostPath definition, but it works for the test
        ));

        final var statefulSet = resources.getStatefulSet(synchronyStatefulSetName());

        assertThat(statefulSet.getVolume("synchrony-home"))
                .hasValueSatisfying(synchronyHomeVolume -> assertThat(synchronyHomeVolume).isObject(Map.of("hostPath", "/foo/bar")));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchronyHome_pvc_custom(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "volumes.synchronyHome.persistentVolumeClaim.create", "true",
                "volumes.synchronyHome.persistentVolumeClaim.storageClassName", "foo",
                "volumes.synchronyHome.persistentVolumeClaim.resources.requests.storage", "2Gi",
                "volumes.synchronyHome.mountPath", "/foo/bar"));

        final var statefulSet = resources.getStatefulSet(synchronyStatefulSetName());

        final var synchronyHomeVolumeClaimTemplate = statefulSet.getVolumeClaimTemplates().head();
        verifyVolumeClaimTemplate(synchronyHomeVolumeClaimTemplate, "synchrony-home", "ReadWriteOnce");
        assertThat(synchronyHomeVolumeClaimTemplate.path("spec").path("storageClassName"))
                .hasTextEqualTo("foo");
        assertThat(synchronyHomeVolumeClaimTemplate.path("spec").path("resources").path("requests").path("storage"))
                .hasTextEqualTo("2Gi");
        final var mount = statefulSet.getContainer("synchrony").getVolumeMount("synchrony-home");
        assertThat(mount.get("name")).hasTextEqualTo("synchrony-home");
        assertThat(mount.get("mountPath")).hasTextEqualTo("/foo/bar");
    }

    private void verifyVolumeClaimTemplate(JsonNode volumeClaimTemplate, final String expectedVolumeName, final String... expectedAccessModes) {
        assertThat(volumeClaimTemplate.path("metadata").path("name"))
                .hasTextEqualTo(expectedVolumeName);
        assertThat(volumeClaimTemplate.path("spec").path("accessModes"))
                .isArrayWithChildren(expectedAccessModes);
    }

}
