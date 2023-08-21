package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

public class AdditionalConfigMapsTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_configmap_configmap_exists(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalConfigMaps[0].name", "extra-configmap",
                "additionalConfigMaps[0].keys[0].fileName", "hello.txt",
                "additionalConfigMaps[0].keys[0].mountPath", "/opt/files",
                "additionalConfigMaps[0].keys[0].content", "helloworld"
        ));

        // assert ConfigMap is created and has expected content in data.key
        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-extra-configmap");
        assertThat(additionalConfigMap.getConfigMapData().get("hello.txt")).hasTextContaining("helloworld");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_configmap_sts_annotation(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalConfigMaps[0].name", "extra-configmap",
                "additionalConfigMaps[0].keys[0].fileName", "hello.txt",
                "additionalConfigMaps[0].keys[0].mountPath", "/opt/files",
                "additionalConfigMaps[0].keys[0].content", "helloworld"
        ));

        // assert there's checksum annotation in spec.template.metadata to make sure sts is restarted when a ConfigMap changes
        StatefulSet statefulset = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulset.getSpec().get("template").get("metadata").get("annotations").get("checksum/config-additional")).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_configmap_volumes(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalConfigMaps[0].name", "extra-configmap",
                "additionalConfigMaps[0].keys[0].fileName", "hello.txt",
                "additionalConfigMaps[0].keys[0].mountPath", "/opt/files",
                "additionalConfigMaps[0].keys[0].content", "helloworld"
        ));

        StatefulSet statefulset = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulset.getVolumes().get(0).get("name")).hasTextEqualTo("hello-txt");

        JsonNode configMapVolume = getStsVolume(statefulset, "configMap");
        assertThat(configMapVolume.path("name")).hasTextEqualTo(product.getHelmReleaseName() + "-extra-configmap");
        assertThat(configMapVolume.path("defaultMode")).isEmpty();
        assertThat(configMapVolume.path("items").path(0).path("key")).hasTextEqualTo("hello.txt");
        assertThat(configMapVolume.path("items").path(0).path("path")).hasTextEqualTo("hello.txt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_configmap_volumes_default_mode(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalConfigMaps[0].name", "extra-configmap",
                "additionalConfigMaps[0].keys[0].defaultMode", "487",
                "additionalConfigMaps[0].keys[0].fileName", "hello.txt",
                "additionalConfigMaps[0].keys[0].mountPath", "/opt/files",
                "additionalConfigMaps[0].keys[0].content", "helloworld"
        ));

        StatefulSet statefulset = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulset.getVolumes().get(0).get("name")).hasTextEqualTo("hello-txt");

        JsonNode configMapVolume = getStsVolume(statefulset, "configMap");
        assertThat(configMapVolume.path("name")).hasTextEqualTo(product.getHelmReleaseName() + "-extra-configmap");
        assertThat(configMapVolume.path("defaultMode")).hasValueEqualTo(487);
        assertThat(configMapVolume.path("items").path(0).path("key")).hasTextEqualTo("hello.txt");
        assertThat(configMapVolume.path("items").path(0).path("path")).hasTextEqualTo("hello.txt");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_configmap_volume_mounts(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalConfigMaps[0].name", "extra-configmap",
                "additionalConfigMaps[0].keys[0].fileName", "hello.txt",
                "additionalConfigMaps[0].keys[0].mountPath", "/opt/files",
                "additionalConfigMaps[0].keys[0].content", "helloworld"
        ));

        StatefulSet statefulset = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode volumeMount = statefulset.getContainer(product.name()).getVolumeMount("hello-txt");

        assertThat(volumeMount.path("name")).hasTextEqualTo("hello-txt");
        assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/opt/files/hello.txt");
        assertThat(volumeMount.path("subPath")).hasTextEqualTo("hello.txt");
    }

    private JsonNode getStsVolume(StatefulSet statefulSet, String secret) {
        return statefulSet
                .getVolume("hello-txt")
                .getOrElseThrow(() -> new AssertionError("custom config map is missing"))
                .path(secret);
    }
}
