package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
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
    @EnumSource
    void fluentd_sidecar_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "fluentd.enabled", "true",
                "fluentd.elasticsearch.hostname", "myelastic"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer("fluentd");

        final var fluentdConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-fluentd-config");
        final var config = fluentdConfigMap.getNode("data", "fluent.conf").asText();

        assertThat(config)
                .contains("host myelastic");
    }

    @ParameterizedTest
    @EnumSource
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
    @EnumSource
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
}
