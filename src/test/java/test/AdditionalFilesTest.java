package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Deployment;
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
    @EnumSource(value = Product.class)
    void additional_files_config_map_creates_volumes(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalFiles[0].name", "custom-config-test",
                "additionalFiles[0].type", "configMap",
                "additionalFiles[0].key", "log4j.properties",
                "additionalFiles[0].mountPath", "/var/atlassian"
        ));

        JsonNode configMap;
        if (product.name().equals("bamboo_agent")) {
            Deployment deployment = resources.getDeployment(product.getHelmReleaseName());
            configMap = getDeploymentVolume(deployment, "configMap");
        } else {
            StatefulSet statefulset = resources.getStatefulSet(product.getHelmReleaseName());
            configMap = getStsVolume(statefulset, "configMap");
        }

        assertThat(configMap.path("name")).hasTextEqualTo("custom-config-test");
        assertThat(configMap.path("items").path(0).path("key")).hasTextEqualTo("log4j.properties");
        assertThat(configMap.path("items").path(0).path("path")).hasTextEqualTo("log4j.properties");
    }


    @ParameterizedTest
    @EnumSource(value = Product.class)
    void additional_files_secret_creates_new_secrets(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.mesh.enabled", "true",
                "additionalFiles[0].name", "custom-config-test",
                "additionalFiles[0].type", "secret",
                "additionalFiles[0].key", "secretKey",
                "additionalFiles[0].mountPath", "/var/secret",
                "bitbucket.mesh.additionalFiles[0].name", "custom-config-test",
                "bitbucket.mesh.additionalFiles[0].type", "secret",
                "bitbucket.mesh.additionalFiles[0].key", "secretKey",
                "bitbucket.mesh.additionalFiles[0].mountPath", "/var/secret"
        ));

        if (product.name().equals("bamboo_agent")) {
            Deployment deployment = resources.getDeployment(product.getHelmReleaseName());
            JsonNode configMap = getDeploymentVolume(deployment, "secret");
            assertThat(configMap.path("secretName")).hasTextEqualTo("custom-config-test");
            assertThat(configMap.path("items").path(0).path("key")).hasTextEqualTo("secretKey");
            assertThat(configMap.path("items").path(0).path("path")).hasTextEqualTo("secretKey");
        } else {
            StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
            StatefulSet[] stsToCheck = {statefulSet};
            if (product.name().contains("bitbucket")) {
                StatefulSet meshStatefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
                stsToCheck = new StatefulSet[]{statefulSet, meshStatefulSet};
            }
            for (var sts : stsToCheck) {
                JsonNode configMap = getStsVolume(sts, "secret");
                assertThat(configMap.path("secretName")).hasTextEqualTo("custom-config-test");
                assertThat(configMap.path("items").path(0).path("key")).hasTextEqualTo("secretKey");
                assertThat(configMap.path("items").path(0).path("path")).hasTextEqualTo("secretKey");
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class)
    void additional_files_configMap_create_volume_mounts(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.mesh.enabled", "true",
                "additionalFiles[0].name", "custom-config-test",
                "additionalFiles[0].type", "secret",
                "additionalFiles[0].key", "secretKey",
                "additionalFiles[0].mountPath", "/var/secret",
                "bitbucket.mesh.additionalFiles[0].name", "custom-config-test",
                "bitbucket.mesh.additionalFiles[0].type", "secret",
                "bitbucket.mesh.additionalFiles[0].key", "secretKey",
                "bitbucket.mesh.additionalFiles[0].mountPath", "/var/secret"
        ));

        String name = "custom-config-test-0";
        if (product.name().equals("bamboo_agent")) {
            Deployment deployment = resources.getDeployment(product.getHelmReleaseName());
            JsonNode volumeMount = deployment.getContainer("bamboo-agent").getVolumeMount(name);
            assertThat(volumeMount.path("name")).hasTextEqualTo("custom-config-test-0");
            assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/var/secret/secretKey");
            assertThat(volumeMount.path("subPath")).hasTextEqualTo("secretKey");
        } else {
            StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
            StatefulSet[] stsToCheck = {statefulSet};
            if (product.name().contains("bitbucket")) {
                StatefulSet meshStatefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
                stsToCheck = new StatefulSet[]{statefulSet, meshStatefulSet};
            }

            for (var sts : stsToCheck) {
                String[] containers = {product.name()};
                if (product.name().contains("bitbucket") && sts.getName().contains("mesh")) {
                    containers = new String[]{product.name() + "-mesh"};
                }
                for (var container : containers) {
                    JsonNode volumeMount = sts.getContainer(container).getVolumeMount(name);
                    assertThat(volumeMount.path("name")).hasTextEqualTo("custom-config-test-0");
                    assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/var/secret/secretKey");
                    assertThat(volumeMount.path("subPath")).hasTextEqualTo("secretKey");
                }
            }
        }
    }

    private JsonNode getStsVolume(StatefulSet statefulSet, String secret) {
        return statefulSet
                .getVolume("custom-config-test-0")
                .getOrElseThrow(() -> new AssertionError("custom config map is missing"))
                .path(secret);
    }
    private JsonNode getDeploymentVolume(Deployment deployment, String secret) {
        return deployment
                .getVolume("custom-config-test-0")
                .getOrElseThrow(() -> new AssertionError("custom config map is missing"))
                .path(secret);
    }
}
