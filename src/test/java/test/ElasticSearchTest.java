package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

class ElasticSearchTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void elastic_search_baseUrl(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".elasticSearch.baseUrl", "https://foo/"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("SEARCH_ENABLED", "false")
                .assertHasValue("PLUGIN_SEARCH_ELASTICSEARCH_BASEURL", "https://foo/");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void elastic_search_credentials(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".elasticSearch.credentials.secretName", "mysecret"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasSecretRef("PLUGIN_SEARCH_ELASTICSEARCH_USERNAME", "mysecret", "username")
                .assertHasSecretRef("PLUGIN_SEARCH_ELASTICSEARCH_PASSWORD", "mysecret", "password");
    }
}
