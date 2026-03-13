package test;

import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.Product;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test.jackson.JsonNodeAssert.assertThat;

class GatewayTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_creates_httproute_with_parent_ref_and_hostname(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoutes = resources.getAll(Kind.HTTPRoute);
        assertEquals(1, httpRoutes.size());

        final var httpRoute = httpRoutes.head();
        assertThat(httpRoute.getNode("spec", "parentRefs").required(0).path("name"))
                .hasTextEqualTo("my-gateway");
        assertThat(httpRoute.getNode("spec", "hostnames").required(0))
                .hasTextEqualTo(product + ".example.com");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_cannot_coexist_with_ingress(Product product) {
        assertThrowsAssertionWithMessage(
                "ERROR: Cannot enable both gateway.create and ingress.create",
                () -> helm.captureKubeResourcesFromHelmChart(product, Map.of(
                        "gateway.create", "true",
                        "ingress.create", "true",
                        "gateway.parentRef.name", "my-gateway",
                        "gateway.hostnames[0]", product + ".example.com"))
        );
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_requires_gateway_name(Product product) {
        assertThrowsAssertionWithMessage(
                "ERROR: gateway.parentRef.name is required when gateway.create is true",
                () -> helm.captureKubeResourcesFromHelmChart(product, Map.of(
                        "gateway.create", "true",
                        "gateway.hostnames[0]", product + ".example.com"))
        );
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_requires_hostnames(Product product) {
        assertThrowsAssertionWithMessage(
                "ERROR: gateway.hostnames must contain at least one hostname when gateway.create is true",
                () -> helm.captureKubeResourcesFromHelmChart(product, Map.of(
                        "gateway.create", "true",
                        "gateway.parentRef.name", "my-gateway"))
        );
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_custom_path_is_applied_to_route_match(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.path", "/" + product));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("matches").required(0).path("path").path("value"))
                .hasTextEqualTo("/" + product);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_supports_multiple_hostnames(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
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
    void gateway_namespace_is_set_on_parent_ref(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.parentRef.namespace", "gateway-system",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "parentRefs").required(0).path("namespace"))
                .hasTextEqualTo("gateway-system");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_supports_all_path_types(Product product) throws Exception {
        for (String pathType : new String[]{"PathPrefix", "Exact", "RegularExpression"}) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "gateway.create", "true",
                    "gateway.parentRef.name", "my-gateway",
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
    void gateway_custom_annotations_are_applied(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.annotations.cert-manager\\.io/cluster-issuer", "letsencrypt"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("metadata", "annotations")
                .path("cert-manager.io/cluster-issuer"))
                .hasTextEqualTo("letsencrypt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_custom_labels_are_applied(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.labels.environment", "production"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("metadata", "labels").path("environment"))
                .hasTextEqualTo("production");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_backend_ref_targets_product_service(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        final var backendRef = httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0);

        assertThat(backendRef.path("name"))
                .hasTextContaining(product.toString());
        assertEquals(80, backendRef.path("port").asInt());
        assertEquals(100, backendRef.path("weight").asInt());
    }

    @Test
    void gateway_routes_synchrony_traffic_when_enabled() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.confluence, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", "confluence.example.com",
                "synchrony.enabled", "true"));

        final var httpRoute = resources.get(Kind.HTTPRoute);

        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("backendRefs").required(0).path("name"))
                .hasTextContaining("synchrony");

        assertThat(httpRoute.getNode("spec", "rules").required(1)
                .path("backendRefs").required(0).path("name"))
                .hasTextContaining("confluence");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_custom_timeouts_are_applied(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com",
                "gateway.timeouts.request", "120s",
                "gateway.timeouts.backendRequest", "60s"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        final var rule = httpRoute.getNode("spec", "rules").required(0);
        assertThat(rule.path("timeouts").path("request"))
                .hasTextEqualTo("120s");
        assertThat(rule.path("timeouts").path("backendRequest"))
                .hasTextEqualTo("60s");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_has_default_timeouts(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        final var rule = httpRoute.getNode("spec", "rules").required(0);
        assertThat(rule.path("timeouts").path("request"))
                .hasTextEqualTo("60s");
        assertThat(rule.path("timeouts").path("backendRequest"))
                .hasTextEqualTo("60s");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent", "crowd"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_default_path_is_root(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("matches").required(0).path("path").path("value"))
                .hasTextEqualTo("/");
    }

    @Test
    void gateway_default_path_uses_context_path_for_crowd() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.crowd, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", "crowd.example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("matches").required(0).path("path").path("value"))
                .hasTextEqualTo("/crowd");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void gateway_default_path_type_is_prefix(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "gateway.create", "true",
                "gateway.parentRef.name", "my-gateway",
                "gateway.hostnames[0]", product + ".example.com"));

        final var httpRoute = resources.get(Kind.HTTPRoute);
        assertThat(httpRoute.getNode("spec", "rules").required(0)
                .path("matches").required(0).path("path").path("type"))
                .hasTextEqualTo("PathPrefix");
    }

    private static void assertThrowsAssertionWithMessage(String expectedErrorMessage, Executable fn) {
        assertThatString(
                assertThrows(AssertionError.class, fn).getMessage()
        ).contains(expectedErrorMessage);
    }
    private static AbstractStringAssert<?> assertThatString(String text) {
        return org.assertj.core.api.Assertions.assertThat(text);
    }
}
