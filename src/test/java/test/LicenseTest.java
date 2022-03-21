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
class LicenseTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_license_secret_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "confluence.license.secretName", "license_secret",
                "confluence.license.secretKey", "mykey"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasSecretRef("ATL_LICENSE_KEY", "license_secret", "mykey");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucket_license_secret_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.license.secretName", "license_secret",
                "bitbucket.license.secretKey", "mykey"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasSecretRef("SETUP_LICENSE", "license_secret", "mykey");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_license_secret_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bamboo.license.secretName", "license_secret",
                "bamboo.license.secretKey", "mykey"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasSecretRef("ATL_LICENSE", "license_secret", "mykey");
    }
}
