package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.jackson.JsonNodeAssert;
import test.model.Product;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static test.model.Kind.ConfigMap;

class FluentdTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_sidecar_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.elasticsearch.hostname", "myelastic"));

        final var fluentdContainer = resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer("fluentd");
        assertThat(fluentdContainer.getResources()).isEmpty();

        final var fluentdConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-fluentd-config");
        final var config = fluentdConfigMap.getNode("data", "fluent.conf").asText();

        assertThat(config)
                .contains("host myelastic");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_uses_default_start_command(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true"
        ));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer("fluentd");

        final var fluentdStartCommand = resources.getStatefulSet(product.getHelmReleaseName()).getContainer("fluentd").get("command").toString();

        assertThat(fluentdStartCommand)
                .contains("fluentd -c /fluentd/etc/fluent.conf -v");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_uses_custom_start_command(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.command", "gem install fluent-plugin-elasticsearch && fluentd -c /fluentd/etc/fluent.conf -v"
        ));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer("fluentd");

        final var fluentdStartCommand = resources.getStatefulSet(product.getHelmReleaseName()).getContainer("fluentd").get("command").toString();

        assertThat(fluentdStartCommand)
                .contains("gem install fluent-plugin-elasticsearch && fluentd -c /fluentd/etc/fluent.conf -v");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_checksum_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "false"
        ));

        final var metadata = resources.getStatefulSet(product.getHelmReleaseName()).getPodMetadata();
        final var checksum = metadata.get("annotations").get("checksum/config-fluentd");

        assertThat(checksum).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_changes_annotation_checksum(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true"
        ));

        final var metadata = resources.getStatefulSet(product.getHelmReleaseName()).getPodMetadata();
        final var checksum = metadata.get("annotations").get("checksum/config-fluentd");

        assertThat(checksum).isNotNull();

        final var resourcesWithChanges = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.elasticsearch.hostname", "myelastic"));

        final var metadataWithChanges = resourcesWithChanges.getStatefulSet(product.getHelmReleaseName()).getPodMetadata();
        final var checksumWithChanges = metadataWithChanges.get("annotations").get("checksum/config-fluentd");

        assertThat(checksumWithChanges)
                .isNotNull()
                .isNotEqualTo(checksum);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_sidecar_enabled_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.resources.requests.cpu", "1",
                "fluentd.resources.requests.memory", "2Gi",
                "fluentd.resources.limits.cpu", "2",
                "fluentd.resources.limits.memory", "3Gi"));

        final var fluentdContainerResources = resources.getStatefulSet(product.getHelmReleaseName()).getContainer("fluentd").getResources();

        assertThat(fluentdContainerResources.path("requests").path("cpu").toString()).contains("1");
        assertThat(fluentdContainerResources.path("requests").path("memory").toString()).contains("2Gi");
        assertThat(fluentdContainerResources.path("limits").path("cpu").toString()).contains("2");
        assertThat(fluentdContainerResources.path("limits").path("memory").toString()).contains("3Gi");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "jira", "crowd"}, mode = EnumSource.Mode.INCLUDE)
    void fluentd_cloud_logging_property_false(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.customConfigFile", "custom\nconfig"));
        final var jvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-jvm-config");
        JsonNodeAssert.assertThat(jvmConfigMap.getConfigMapData().path("additional_jvm_args")).hasTextContaining("-Datlassian.logging.cloud.enabled=false");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "jira", "crowd"}, mode = EnumSource.Mode.INCLUDE)
    void fluentd_cloud_logging_property_true(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true"));
        final var jvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-jvm-config");
        JsonNodeAssert.assertThat(jvmConfigMap.getConfigMapData().path("additional_jvm_args")).hasTextContaining("-Datlassian.logging.cloud.enabled=true");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent", "jira", "confluence"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_local_volumes(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true"));

        final var sts = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode volumeMount = sts.getContainer("fluentd").getVolumeMount("local-home");

        JsonNodeAssert.assertThat(volumeMount.path("name")).hasTextEqualTo("local-home");
        JsonNodeAssert.assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/application-data/logs");
        Assertions.assertThat(volumeMount.path("readOnly").booleanValue()).isEqualTo(true);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void fluentd_extra_volumes(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.extraVolumes[0].name", "custom",
                "fluentd.extraVolumes[0].mountPath", "/var/atlassian/product/logs",
                "fluentd.extraVolumes[0].readOnly", "true"));

        final var sts = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode volumeMount = sts.getContainer("fluentd").getVolumeMount("custom");

        JsonNodeAssert.assertThat(volumeMount.path("name")).hasTextEqualTo("custom");
        JsonNodeAssert.assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/var/atlassian/product/logs");
        Assertions.assertThat(volumeMount.path("readOnly").booleanValue()).isEqualTo(true);
    }
}
