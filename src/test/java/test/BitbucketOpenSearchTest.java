package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.jackson.JsonNodeAssert;
import test.model.Product;

import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.ConfigMap;
import static test.model.Kind.Secret;

class BitbucketOpenSearchTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_statefulset_exists(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.install", "true"
        ));
        final var statefulSet = resources.getStatefulSet("opensearch-cluster-master");
        assertThat(statefulSet.getSpec()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_existing_external(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.baseUrl", "https://opensearchinstance.info",
                "bitbucket.clustering.enabled", "true"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        statefulSet.getContainer("bitbucket").getEnv().assertHasValue("PLUGIN_SEARCH_CONFIG_BASEURL", "https://opensearchinstance.info");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_container_env_vars_defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.install", "true",
                "bitbucket.clustering.enabled", "true"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        // we use an initial admin credentials here
        statefulSet.getContainer("bitbucket").getEnv().assertHasValue("PLUGIN_SEARCH_CONFIG_BASEURL", "http://opensearch-cluster-master:9200");
        statefulSet.getContainer("bitbucket").getEnv().assertHasValue("PLUGIN_SEARCH_CONFIG_USERNAME", "admin");
        statefulSet.getContainer("bitbucket").getEnv().assertHasSecretRef("PLUGIN_SEARCH_CONFIG_PASSWORD", "opensearch-initial-password", "OPENSEARCH_INITIAL_ADMIN_PASSWORD");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_container_env_vars_custom_secret(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.install", "true",
                "opensearch.credentials.secretName", "custom-secret",
                "opensearch.credentials.usernameSecretKey", "user",
                "opensearch.credentials.passwordSecretKey", "pass",
                "bitbucket.clustering.enabled", "true"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        statefulSet.getContainer("bitbucket").getEnv().assertHasSecretRef("PLUGIN_SEARCH_CONFIG_USERNAME", "custom-secret", "user");
        statefulSet.getContainer("bitbucket").getEnv().assertHasSecretRef("PLUGIN_SEARCH_CONFIG_PASSWORD", "custom-secret", "pass");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_container_env_vars_custom_url(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.install", "true",
                "opensearch.baseUrl", "https://opensearch-cluster-master:9200",
                "bitbucket.clustering.enabled", "true"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        // we use an initial admin credentials here
        statefulSet.getContainer("bitbucket").getEnv().assertHasValue("PLUGIN_SEARCH_CONFIG_BASEURL", "https://opensearch-cluster-master:9200");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_secret_has_password(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.install", "true"
        ));
        final var secret = resources.get(Secret, "opensearch-initial-password");
        JsonNode password = secret.getConfigMapData().path("OPENSEARCH_INITIAL_ADMIN_PASSWORD");
        JsonNodeAssert.assertThat(password).isNotNull();
        assertDoesNotThrow(() -> {
            Base64.getDecoder().decode(password.asText());
        }, "Password should be a valid Base64 encoded string");
        byte[] decodedPassword = Base64.getDecoder().decode(password.asText());
        assertEquals(40, decodedPassword.length, "The decoded password should have a length of 40 bytes.");
    }
}
