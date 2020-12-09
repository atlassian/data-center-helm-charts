package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.Product;

import java.util.Map;

import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static test.jackson.JsonNodeAssert.assertThat;

class IngressTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource
    void ingress_create(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain"));

        final var ingress = resources.get(Kind.Ingress);

        assertThat(ingress.getNode("spec", "rules").required(0).path("host"))
                .hasTextContaining("myhost.mydomain");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, mode = EXCLUDE, names = "bitbucket")
    void https_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.secure", "false",
                "ingress.scheme", "http"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_TOMCAT_SCHEME", "http")
                .assertHasValue("ATL_TOMCAT_SECURE", "false");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void https_disabled_bitbucket(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.secure", "false",
                "ingress.scheme", "http"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("SERVER_SCHEME", "http")
                .assertHasValue("SERVER_SECURE", "false");
    }
}
