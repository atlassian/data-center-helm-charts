package test;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Array;
import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;
import test.model.StatefulSet;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.Job;
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
                .describedAs("StatefulSet %s should have a single volumeClaimTempate", statefulSet.getName())
                .hasSize(1);

        verifyVolumeClaimTemplate(
                statefulSet.getVolumeClaimTemplates().head(),
                "local-home", "ReadWriteOnce");

        assertThat(getVolume(statefulSet, "local-home"))
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

        assertThat(getVolume(statefulSet, "shared-home"))
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

        assertThat(getVolume(statefulSet, "local-home"))
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

        assertThat(getVolume(statefulSet, "shared-home"))
                .hasValueSatisfying(localHomeVolume -> assertThat(localHomeVolume).isObject(Map.of("hostPath", "/foo/bar")));
    }

    @ParameterizedTest
    @EnumSource
    void sharedHome_permissionFixer_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.nfsPermissionFixer.enabled", "true"
        ));

        final var fixerCommand = resources.get(Job, product.getHelmReleaseName() + "-nfs-fixer")
                .getNode("spec", "template", "spec", "containers")
                .required(0)
                .required("command");

        final var fixerCommandString = Array.ofAll(fixerCommand)
                .map(JsonNode::asText)
                .mkString(" ");

        assertThat(fixerCommandString)
                .isEqualTo("sh -c (chgrp %s /shared-home; chmod g+w /shared-home)", product.getContainerGid());
    }


    private void verifyVolumeClaimTemplate(JsonNode volumeClaimTemplate, final String expectedVolumeName, final String... expectedAccessModes) {
        assertThat(volumeClaimTemplate.path("metadata").path("name"))
                .hasTextEqualTo(expectedVolumeName);
        assertThat(volumeClaimTemplate.path("spec").path("accessModes"))
                .isArrayWithChildren(expectedAccessModes);
    }

    private Option<JsonNode> getVolume(StatefulSet statefulSet, final String volumeName) {
        final var volumes = statefulSet.getPodSpec().required("volumes");
        return Array.ofAll(volumes)
                .find(volume -> volume.path("name").asText().equals(volumeName));
    }
}
