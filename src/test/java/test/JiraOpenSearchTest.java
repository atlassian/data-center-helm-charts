package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import test.helm.Helm;
import test.model.Product;

import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.Secret;

class JiraOpenSearchTest {

    private static final Product JIRA = Product.jira;

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @Test
    void opensearch_statefulset_is_created_when_enabled() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of(
                "opensearch.enabled", "true"
        ));
        final var statefulSet = resources.getStatefulSet("opensearch-cluster-master");
        assertThat(statefulSet.getSpec()).isNotNull();
    }

    @Test
    void opensearch_secret_contains_valid_base64_password() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of(
                "opensearch.enabled", "true"
        ));
        final var secret = resources.get(Secret, "opensearch-initial-password");
        JsonNode password = secret.getConfigMapData().path("OPENSEARCH_INITIAL_ADMIN_PASSWORD");
        assertThat(password).isNotNull();
        assertDoesNotThrow(() -> {
            Base64.getDecoder().decode(password.asText());
        }, "Password should be a valid Base64 encoded string");
        byte[] decodedPassword = Base64.getDecoder().decode(password.asText());
        assertEquals(40, decodedPassword.length, "The decoded password should have a length of 40 bytes.");
    }

    @Test
    void opensearch_env_vars_are_set_with_default_credentials() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of(
                "opensearch.enabled", "true"
        ));

        final var statefulSet = resources.getStatefulSet(JIRA.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_SEARCH_PLATFORM", "search.platform=opensearch");
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_SEARCH_URL", "opensearch.http.url=http://opensearch-cluster-master:9200");
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_SEARCH_USERNAME", "opensearch.username=admin");
        env.assertHasSecretRef("OPENSEARCH_ADMIN_PASSWORD", "opensearch-initial-password", "OPENSEARCH_INITIAL_ADMIN_PASSWORD");
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_SEARCH_PASSWORD__EXPAND_ENV", "opensearch.password={OPENSEARCH_ADMIN_PASSWORD}");
    }

    @Test
    void opensearch_env_vars_use_existing_secret_when_configured() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of(
                "opensearch.enabled", "true",
                "opensearch.credentials.existingSecretRef.name", "my-opensearch-secret"
        ));

        final var statefulSet = resources.getStatefulSet(JIRA.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasSecretRef("OPENSEARCH_ADMIN_PASSWORD", "my-opensearch-secret", "OPENSEARCH_INITIAL_ADMIN_PASSWORD");
    }
}
