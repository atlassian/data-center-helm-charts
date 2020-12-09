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

        assertThat(ingress.getMetadata().path("annotations"))
                .isObject(Map.of("kubernetes.io/ingress.class", "nginx"));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucket_ingress_host(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("SERVER_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("SERVER_PROXY_PORT", "443")
                .assertHasValue("SETUP_BASEURL", "https://myhost.mydomain");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucket_ingress_host_port(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain",
                "ingress.port", "666"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("SERVER_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("SERVER_PROXY_PORT", "666")
                .assertHasValue("SETUP_BASEURL", "https://myhost.mydomain:666");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, mode = EXCLUDE, names = "bitbucket")
    void https_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.https", "false"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertDoesNotHaveAnyOf("ATL_TOMCAT_SCHEME", "ATL_TOMCAT_SECURE");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void https_disabled_bitbucket(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.https", "false"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertDoesNotHaveAnyOf("SERVER_SCHEME", "SERVER_SECURE");
    }
}
