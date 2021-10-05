package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

/**
 * Tests the various permutations of the "database" value structure in the Helm charts
 */
class DatabaseTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_database(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.url", "myurl",
                "database.type", "mytype",
                "database.driver", "mydriver",
                "database.credentials.secretName", "mysecret",
                "database.credentials.usernameSecretKey", "myusername",
                "database.credentials.passwordSecretKey", "mypassword"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_JDBC_URL", "myurl")
                .assertHasValue("ATL_DB_TYPE", "mytype")
                .assertHasValue("ATL_DB_DRIVER", "mydriver")
                .assertHasSecretRef("ATL_JDBC_USER", "mysecret", "myusername")
                .assertHasSecretRef("ATL_JDBC_PASSWORD", "mysecret", "mypassword");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucket_database(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.url", "myurl",
                "database.driver", "mydriver",
                "database.credentials.secretName", "mysecret",
                "database.credentials.usernameSecretKey", "myusername",
                "database.credentials.passwordSecretKey", "mypassword"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("JDBC_URL", "myurl")
                .assertHasValue("JDBC_DRIVER", "mydriver")
                .assertHasSecretRef("JDBC_USER", "mysecret", "myusername")
                .assertHasSecretRef("JDBC_PASSWORD", "mysecret", "mypassword");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_database(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.url", "myurl",
                "database.type", "mytype",
                "database.driver", "mydriver",
                "database.credentials.secretName", "mysecret",
                "database.credentials.usernameSecretKey", "myusername",
                "database.credentials.passwordSecretKey", "mypassword"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_JDBC_URL", "myurl")
                .assertHasValue("ATL_DB_TYPE", "mytype")
                .assertHasSecretRef("ATL_JDBC_USER", "mysecret", "myusername")
                .assertHasSecretRef("ATL_JDBC_PASSWORD", "mysecret", "mypassword");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_database(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.url", "myurl",
                "database.type", "mytype",
                "database.driver", "mydriver",
                "database.credentials.secretName", "mysecret",
                "database.credentials.usernameSecretKey", "myusername",
                "database.credentials.passwordSecretKey", "mypassword"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_JDBC_URL", "myurl")
                .assertHasValue("ATL_DB_TYPE", "mytype")
                .assertHasSecretRef("ATL_JDBC_USER", "mysecret", "myusername")
                .assertHasSecretRef("ATL_JDBC_PASSWORD", "mysecret", "mypassword");
    }
}
