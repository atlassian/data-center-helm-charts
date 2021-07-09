package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.KubeResource;
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
                "ingress.host", "myhost.mydomain",
                "ingress.path", "/mypath"));

        final var ingresses = resources.getAll(Kind.Ingress);

        for (KubeResource ingress : ingresses) {
            assertThat(ingress.getNode("spec", "rules").required(0).path("host"))
                    .hasTextContaining("myhost.mydomain");

            assertThat(ingress.getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                    .hasTextContaining("/mypath");

            assertThat(ingress.getMetadata().path("annotations"))
                    .isObject(Map.of("kubernetes.io/ingress.class", "nginx"));
        }
    }

    @ParameterizedTest
    @EnumSource
    void ingress_create_tls (Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.tlsSecretName", "tls-secret",
                "ingress.host", "myhost.mydomain"));

        final var ingresses = resources.getAll(Kind.Ingress);
        for (KubeResource ingress : ingresses) {
            assertThat(ingress.getNode("spec", "tls").required(0).path("hosts").required(0))
                    .hasTextContaining("myhost.mydomain");

            assertThat(ingress.getNode("spec", "tls").required(0).path("secretName"))
                    .hasTextContaining("tls-secret");
        }
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

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_has_exactly_2_ingresses(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain"));

        final var ingresses = resources.getAll(Kind.Ingress);
        // This is because Connie provisions a regular ingress + an ingress for /setup paths with increased timeout
        Assertions.assertEquals(2, ingresses.size());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_ingress_host_port(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain",
                "ingress.port", "666"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("ATL_PROXY_PORT", "666");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_ingress_port(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("ATL_PROXY_PORT", "443");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_ingress_port_http(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain",
                "ingress.https", "False"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("ATL_PROXY_PORT", "80");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_ingress_path_contextPath(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "jira.service.contextPath", "/jira-tmp"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertNotEquals(0, ingresses.size());

        for (KubeResource ingress : ingresses) {
            assertThat(ingress.getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                    .hasTextContaining("/jira-tmp");
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_ingress_path_value(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "jira.service.contextPath", "/context",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertNotEquals(0, ingresses.size());

        for (KubeResource ingress : ingresses) {
            assertThat(ingress.getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                    .hasTextContaining("/ingress");
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_ingress_path_default(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertNotEquals(0, ingresses.size());

        for (KubeResource ingress : ingresses) {
            assertThat(ingress.getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                    .hasTextContaining("/");
        }
    }

}
