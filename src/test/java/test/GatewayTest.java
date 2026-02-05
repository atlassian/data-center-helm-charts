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

import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests for Gateway API HTTPRoute resources.
 * These tests verify that HTTPRoute resources are correctly generated
 * from Helm chart values and follow Gateway API specifications.
 */
class GatewayTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_create(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoutes = resources.getAll(Kind.HTTPRoute);
        Assertions.assertEquals(1, httpRoutes.size());

        final var httpRoute = httpRoutes.head();
        assertThat(httpRoute.getNode("spec", "parentRefs").required(0).path("name"))
                .hasTextEqualTo("my-gateway");
        assertThat(httpRoute.getNode("spec", "hostnames").required(0))
                .hasTextEqualTo(product + ".example.com");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_cannot_coexist_with_ingress(Product product) {
        Assertions.assertThrows(AssertionError.class, () -> {
            helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "gateway.create", "true",
                    "ingress.create", "true",
                    "gateway.gatewayName", "my-gateway",
                    "gateway.hostnames[0]", product + ".example.com"));
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_requires_gateway_name(Product product) {
        Assertions.assertThrows(AssertionError.class, () -> {
            helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "gateway.create", "true",
                    "gateway.hostnames[0]", product + ".example.com"));
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_requires_hostnames(Product product) {
        Assertions.assertThrows(AssertionError.class, () -> {
            helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "gateway.create", "true",
                    "gateway.gatewayName", "my-gateway"));
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_with_custom_path(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.path", "/" + product));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("matches").required(0).path("path").path("value"))
                .hasTextEqualTo("/" + product);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_with_multiple_hostnames(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.hostnames[1]", product + "-alt.example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "hostnames").required(0))
                .hasTextEqualTo(product + ".example.com");
        assertThat(httpRoute.getNode("spec", "hostnames").required(1))
                .hasTextEqualTo(product + "-alt.example.com");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_with_namespace(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.gatewayNamespace", "gateway-system",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "parentRefs").required(0).path("namespace"))
                .hasTextEqualTo("gateway-system");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_path_types(Product product) throws Exception {
        for (String pathType : new String[]{"PathPrefix", "Exact", "RegularExpression"}) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "gateway.create", "true",
                    "gateway.gatewayName", "my-gateway",
                    "gateway.hostnames[0]", product + ".example.com",
                    "gateway.pathType", pathType));

            final var httpRoute = resources.get(Kind.HTTPRoute);
            assertThat(httpRoute.getNode("spec", "rules").required(0)
                    .path("matches").required(0).path("path").path("type"))
                    .hasTextEqualTo(pathType);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_with_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.annotations.cert-manager\\.io/cluster-issuer", "letsencrypt"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("metadata", "annotations")
                .path("cert-manager.io/cluster-issuer"))
                .hasTextEqualTo("letsencrypt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_with_labels(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.labels.environment", "production"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("metadata", "labels").path("environment"))
                .hasTextEqualTo("production");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"})
    void gateway_backend_refs_bitbucket(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", "bitbucket.example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("name"))
                .hasTextContaining("bitbucket");
        // Service port is 80 (Kubernetes service port), not the container port
        Assertions.assertEquals(80, httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("port").asInt());
        Assertions.assertEquals(100, httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("weight").asInt());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"})
    void gateway_backend_refs_jira(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", "jira.example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        // Service port is 80 (Kubernetes service port), not the container port
        Assertions.assertEquals(80, httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("port").asInt());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void gateway_backend_refs_confluence(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", "confluence.example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        // Service port is 80 (Kubernetes service port), not the container port
        Assertions.assertEquals(80, httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("port").asInt());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo"})
    void gateway_backend_refs_bamboo(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", "bamboo.example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        // Service port is 80 (Kubernetes service port), not the container port
        Assertions.assertEquals(80, httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("port").asInt());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"crowd"})
    void gateway_backend_refs_crowd(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", "crowd.example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        // Service port is 80 (Kubernetes service port), not the container port
        Assertions.assertEquals(80, httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("port").asInt());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_default_path_is_root(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("matches").required(0).path("path").path("value"))
                .hasTextEqualTo("/");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_default_pathType_is_prefix(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.gatewayName", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("matches").required(0).path("path").path("type"))
                .hasTextEqualTo("PathPrefix");
    }
}
