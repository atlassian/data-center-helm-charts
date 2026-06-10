package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class AdditionalConfigPropertiesTest {

    private static final Product JIRA = Product.jira;

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @Test
    void no_additional_config_properties_by_default() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of());

        final var statefulSet = resources.getStatefulSet(JIRA.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertDoesNotHaveAnyOf(
                "ADDITIONAL_JIRA_CONFIG_HELM_000",
                "ADDITIONAL_JIRA_CONFIG_HELM_000__EXPAND_ENV"
        );
    }

    @Test
    void additional_config_properties_are_set() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of(
                "jira.additionalConfigProperties[0]", "jira.websudo.is.disabled=true",
                "jira.additionalConfigProperties[1]", "jira.lf.top.bgcolour=#003366"
        ));

        final var statefulSet = resources.getStatefulSet(JIRA.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_HELM_000", "jira.websudo.is.disabled=true");
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_HELM_001", "jira.lf.top.bgcolour=#003366");
    }

    @Test
    void additional_config_properties_expand_env_are_set() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of(
                "jira.additionalConfigPropertiesExpandEnv[0]", "opensearch.password={OPENSEARCH_INITIAL_ADMIN_PASSWORD}"
        ));

        final var statefulSet = resources.getStatefulSet(JIRA.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_HELM_000__EXPAND_ENV", "opensearch.password={OPENSEARCH_INITIAL_ADMIN_PASSWORD}");
    }

    @Test
    void both_config_properties_and_expand_env_are_set() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(JIRA, Map.of(
                "jira.additionalConfigProperties[0]", "some.property=value1",
                "jira.additionalConfigPropertiesExpandEnv[0]", "secret.property={MY_SECRET}"
        ));

        final var statefulSet = resources.getStatefulSet(JIRA.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_HELM_000", "some.property=value1");
        env.assertHasValue("ADDITIONAL_JIRA_CONFIG_HELM_000__EXPAND_ENV", "secret.property={MY_SECRET}");
    }
}
