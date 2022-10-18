package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;


/**
 * Tests the various permutations of the "<product>.license" value structure in the Helm charts
 */
class ForcedConfigUpdateTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence", "bamboo"})
    void jira_atl_force_config_update_true(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "jira.forceConfigUpdate", "true",
                "bamboo.forceConfigUpdate", "true",
                "confluence.forceConfigUpdate", "true"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_FORCE_CFG_UPDATE", "true");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_atl_force_config_update_false(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "jira.forceConfigUpdate", "false"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertDoesNotHaveAnyOf("ATL_FORCE_CFG_UPDATE");
    }
}
