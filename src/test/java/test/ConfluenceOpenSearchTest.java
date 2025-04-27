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

import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.ConfigMap;
import static test.model.Kind.Secret;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfluenceOpenSearchTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_statefulset_exists(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.enabled", "true"
        ));
        final var statefulSet = resources.getStatefulSet("opensearch-cluster-master");
        assertThat(statefulSet.getSpec()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void jvm_config_map_has_opensearch_args(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.enabled", "true"
        ));
        final var jvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-jvm-config");
        JsonNodeAssert.assertThat(jvmConfigMap.getConfigMapData().path("additional_jvm_args")).hasTextContaining("-Dsearch.platform=opensearch -Dopensearch.http.url=http://opensearch-cluster-master:9200 -Dopensearch.username=admin");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void opensearch_secret_has_password(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.enabled", "true"
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

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void confluence_has_opensearch_envs(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.enabled", "true"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("ATL_SEARCH_PLATFORM", "opensearch");
        env.assertHasValue("ATL_OPENSEARCH_HTTP_URL", "http://opensearch-cluster-master:9200");
        env.assertHasValue("ATL_OPENSEARCH_USERNAME", "admin");
        env.assertHasSecretRef("ATL_OPENSEARCH_PASSWORD", "opensearch-initial-password", "OPENSEARCH_INITIAL_ADMIN_PASSWORD");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void confluence_has_opensearch_env_existing_secret(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "opensearch.enabled", "true",
                "opensearch.credentials.existingSecretRef.name", "my-opensearch-secret"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasSecretRef("ATL_OPENSEARCH_PASSWORD", "my-opensearch-secret", "OPENSEARCH_INITIAL_ADMIN_PASSWORD");
    }
}
