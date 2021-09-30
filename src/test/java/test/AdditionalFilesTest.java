package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;
import test.model.StatefulSet;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests the various permutations of the "<product>.service" value structure in the Helm charts
 */
class AdditionalFilesTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_files_config_map_creates_volumes(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalFiles[0].name", "custom-config-test",
                "additionalFiles[0].type", "configMap",
                "additionalFiles[0].key", "log4j.properties",
                "additionalFiles[0].mountPath", "/var/atlassian"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode configMap = getVolume(statefulSet, "configMap");

        assertThat(configMap.path("name")).hasTextEqualTo("custom-config-test");
        assertThat(configMap.path("items").path(0).path("key")).hasTextEqualTo("log4j.properties");
        assertThat(configMap.path("items").path(0).path("path")).hasTextEqualTo("log4j.properties");
    }


    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_files_secret_creates_new_secrets(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalFiles[0].name", "custom-config-test",
                "additionalFiles[0].type", "secret",
                "additionalFiles[0].key", "secretKey",
                "additionalFiles[0].mountPath", "/var/secret"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode configMap = getVolume(statefulSet, "secret");

        assertThat(configMap.path("secretName")).hasTextEqualTo("custom-config-test");
        assertThat(configMap.path("items").path(0).path("key")).hasTextEqualTo("secretKey");
        assertThat(configMap.path("items").path(0).path("path")).hasTextEqualTo("secretKey");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_files_configMap_create_volume_mounts(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalFiles[0].name", "custom-config-test",
                "additionalFiles[0].type", "secret",
                "additionalFiles[0].key", "secretKey",
                "additionalFiles[0].mountPath", "/var/secret"
        ));

        String name = "custom-config-test-0";

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode volumeMount = statefulSet.getContainer(product.name()).getVolumeMount(name);

        assertThat(volumeMount.path("name")).hasTextEqualTo("custom-config-test-0");
        assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/var/secret/secretKey");
        assertThat(volumeMount.path("subPath")).hasTextEqualTo("secretKey");
    }

    private JsonNode getVolume(StatefulSet statefulSet, String secret) {
        return statefulSet
                .getVolume("custom-config-test-0")
                .getOrElseThrow(() -> new AssertionError("custom config map is missing"))
                .path(secret);
    }
}
