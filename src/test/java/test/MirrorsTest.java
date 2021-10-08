package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

/**
 * Tests the various permutations of the "persistence" value structure in the
 * Helm charts
 */
class MirrorsTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void applicationMode(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(pname + ".applicationMode", "mirror", pname + ".mirror.upstreamUrl", "https://upstream.com"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("APPLICATION_MODE", "mirror");
        env.assertHasValue("PLUGIN_MIRRORING_UPSTREAM_URL", "https://upstream.com");
        env.assertHasValue("PLUGIN_MIRRORING_UPSTREAM_TYPE", "server");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void defaultMode(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(pname + ".applicationMode", "default"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasValue("APPLICATION_MODE", "default");
    }
}
