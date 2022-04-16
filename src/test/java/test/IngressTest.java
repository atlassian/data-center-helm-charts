package test;

import io.vavr.collection.Traversable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.KubeResource;
import test.model.Product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static test.jackson.JsonNodeAssert.assertThat;

class IngressTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
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
    void ingress_create_with_custom_class_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.className", "my-custom-nginx"));

        final var ingresses = resources.getAll(Kind.Ingress);

        for (KubeResource ingress : ingresses) {
            assertThat(ingress.getMetadata().path("annotations"))
                    .isObject(Map.of("kubernetes.io/ingress.class", "my-custom-nginx"));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
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
    @EnumSource(value = Product.class, names = {"bamboo_agent", "bitbucket"}, mode = EnumSource.Mode.EXCLUDE)
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
    @EnumSource(value = Product.class, names = {"jira", "confluence", "bamboo"})
    void jira_ingress_host_port(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain",
                "ingress.port", "666"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("ATL_PROXY_PORT", "666");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence", "bamboo"})
    void jira_ingress_port(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("ATL_PROXY_PORT", "443");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence", "bamboo"})
    void jira_ingress_port_http(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.host", "myhost.mydomain",
                "ingress.https", "False"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_PROXY_NAME", "myhost.mydomain")
                .assertHasValue("ATL_PROXY_PORT", "80");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucket_ingress_path_contextPath(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.path", "/bitbucket"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/bitbucket");
        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("SERVER_CONTEXT_PATH", "/bitbucket");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "bitbucket"})
    void jira_ingress_path_contextPath(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                product + ".service.contextPath", "/my-path"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/my-path");

    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "bitbucket"})
    void jira_ingress_path_value(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                product + ".service.contextPath", "/context",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/ingress");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "bitbucket"})
    void jira_ingress_path_no_context_value(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/ingress");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_ingress_path_default(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_ingress_path_contextPath(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "bamboo.service.contextPath", "/bamboo-tmp"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/bamboo-tmp");

    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_ingress_path_value(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "bamboo.service.contextPath", "/context",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/ingress");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_ingress_path_no_context_value(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/ingress");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_ingress_path_default(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(1, ingresses.size());

        assertThat(ingresses.head().getNode("spec", "rules").required(0).path("http").path("paths").required(0).path("path"))
                .hasTextEqualTo("/");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_atl_base_path_when_all_ingress_vals(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.https", "true",
                "ingress.path", "/bamboo"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_BASE_URL", "https://myhost.mydomain/bamboo");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_atl_base_path_when_no_ingress_path(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.https", "true"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_BASE_URL", "https://myhost.mydomain");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_atl_base_path_when_no_ingress_host(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.https", "true"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_BASE_URL", "http://localhost:8085/");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo")
    void bamboo_atl_base_path_when_ingress_host_over_http(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.https", "false",
                "ingress.path", "/bamboo"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv()
                .assertHasValue("ATL_BASE_URL", "http://myhost.mydomain/bamboo");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_contextPath(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "confluence.service.contextPath", "/confluence-tmp"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(2, ingresses.size());

        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/confluence-tmp",
                "/confluence-tmp/synchrony",
                "/confluence-tmp/setup",
                "/confluence-tmp/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_contextPath_synchronyDisabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "false",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "confluence.service.contextPath", "/confluence-tmp"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(2, ingresses.size());

        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/confluence-tmp",
                "/confluence-tmp/setup",
                "/confluence-tmp/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_value(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "confluence.service.contextPath", "/context",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/ingress",
                "/ingress/synchrony",
                "/ingress/setup",
                "/ingress/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_value_synchronyDisabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "false",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "confluence.service.contextPath", "/context",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/ingress",
                "/ingress/setup",
                "/ingress/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_no_context_value(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(2, ingresses.size());

        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/ingress",
                "/ingress/synchrony",
                "/ingress/setup",
                "/ingress/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_no_context_value_synchronyDisabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "false",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain",
                "ingress.path", "/ingress"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(2, ingresses.size());

        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/ingress",
                "/ingress/setup",
                "/ingress/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_default(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(2, ingresses.size());

        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/",
                "/synchrony",
                "/setup",
                "/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_ingress_path_default_synchronyDisabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "false",
                "ingress.create", "true",
                "ingress.host", "myhost.mydomain"));

        final var ingresses = resources.getAll(Kind.Ingress);
        Assertions.assertEquals(2, ingresses.size());

        final List<String> ingressPaths = extractAllPaths(ingresses);

        org.assertj.core.api.Assertions.assertThat(ingressPaths).containsExactlyInAnyOrder(
                "/",
                "/setup",
                "/bootstrap");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo", "bitbucket", "confluence", "crowd", "jira"})
    void ingress_proxy_settings(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true"));

        final var ingresses = resources.getAll(Kind.Ingress);

        for (KubeResource ingress : ingresses) {
                if ( ingress.getMetadata().path("name").asText().contains("-setup") ) {
                    assertThat(ingress.getMetadata().path("annotations"))
                        .isObject(Map.of(
                            "nginx.ingress.kubernetes.io/proxy-connect-timeout", "300",
                            "nginx.ingress.kubernetes.io/proxy-read-timeout", "300",
                            "nginx.ingress.kubernetes.io/proxy-send-timeout", "300"));
                } else {
                    assertThat(ingress.getMetadata().path("annotations"))
                        .isObject(Map.of(
                            "nginx.ingress.kubernetes.io/proxy-connect-timeout", "60",
                            "nginx.ingress.kubernetes.io/proxy-read-timeout", "60",
                            "nginx.ingress.kubernetes.io/proxy-send-timeout", "60"));
                }
        }
    }

    private List<String> extractAllPaths(Traversable<KubeResource> ingresses) {
        return ingresses
                .flatMap(ingress -> ingress.getNode("spec", "rules"))
                .flatMap(rule -> rule.path("http").path("paths"))
                .map(path -> path.path("path").asText())
                .collect(Collectors.toList());
    }
}
