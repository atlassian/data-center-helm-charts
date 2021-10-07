package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.model.Synchrony.synchronyStatefulSetName;

/**
 * Tests the various permutations of the "<product>.setPermissions" value structure in the Helm charts
 */
class SetPermissionTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @CsvSource({
            "confluence,true",
            "jira,false",
            "bitbucket,true",
            "crowd,false",
            "bamboo,true"
    })
    void test_set_permissions(String productName, String setPermission) throws Exception {
        final var product = Product.valueOf(productName);
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".setPermissions", setPermission));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("SET_PERMISSIONS", setPermission);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void test_set_permissions_synchrony(String setPermission) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.confluence, Map.of(
                "synchrony.enabled", "true",
                "synchrony.setPermissions", setPermission));

        resources.getStatefulSet(synchronyStatefulSetName())
                .getContainer()
                .getEnv()
                .assertHasValue("SET_PERMISSIONS", setPermission);
    }
}
