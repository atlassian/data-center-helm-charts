package test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import test.helm.Helm;
import test.model.Kind;
import test.model.KubeResource;
import test.model.KubeResources;
import test.model.Product;

import java.util.Map;
import java.util.stream.Collectors;

import static test.jackson.JsonNodeAssert.assertThat;

class ConfluenceDebugFlagTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @Test
    void withoutDebugFlagJvmArgsFormOneString() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.confluence, Map.of(
                "confluence.jvmDebug.enabled", "false"));

        final KubeResource configMap = findJvmArgumentsConfigMap(resources);

        assertThat(configMap.getNode("data", "additional_jvm_args"))
                .hasTextNotContaining("\n");

        assertThat(configMap.getNode("data", "additional_jvm_args"))
                .hasTextNotContaining("-Xdebug");
        assertThat(configMap.getNode("data", "additional_jvm_args"))
                .hasTextNotContaining("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005");
    }

    @Test
    void debugFlagArgumentsAreWellFormed() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.confluence, Map.of(
                "confluence.jvmDebug.enabled", "true"));

        final KubeResource configMap = findJvmArgumentsConfigMap(resources);

        assertThat(configMap.getNode("data", "additional_jvm_args"))
                .hasTextNotContaining("\n");

        // The parameter should be surrounded by space characters
        assertThat(configMap.getNode("data", "additional_jvm_args"))
                .hasSeparatedTextContaining("-Xdebug");

        assertThat(configMap.getNode("data", "additional_jvm_args"))
                .hasSeparatedTextContaining("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005");
    }

    private KubeResource findJvmArgumentsConfigMap(KubeResources resources) {
        final var jvmConfigMaps = resources.getAll(Kind.ConfigMap)
                .find(map -> map.getName().endsWith("jvm-config"))
                .collect(Collectors.toList());
        Assertions.assertThat(jvmConfigMaps).hasSize(1);
        return jvmConfigMaps.get(0);
    }
}
