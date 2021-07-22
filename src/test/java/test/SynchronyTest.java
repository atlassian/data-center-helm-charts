package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.Product;
import test.model.StatefulSet;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class SynchronyTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_enable(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.ingressUrl", "https://mysynchrony"
        ));

        resources.assertContains(Kind.StatefulSet, product.getHelmReleaseName() + "-synchrony");
        resources.assertContains(Kind.Service, product.getHelmReleaseName() + "-synchrony");

        final var sysProps = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-jvm-config")
                .getNode("data", "additional_jvm_args");

        assertThat(sysProps)
                .hasTextContaining("-Dsynchrony.service.url=https://mysynchrony/v1")
                .hasTextNotContaining("synchrony.btf.disabled");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "false"
        ));

        final var sysProps = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-jvm-config")
                .getNode("data", "additional_jvm_args");

        assertThat(sysProps).hasTextContaining("synchrony.btf.disabled");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_entrypoint(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true"
        ));

        final var entrypoint = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-synchrony-entrypoint")
                .getNode("data", "start-synchrony.sh");

        assertThat(entrypoint)
                .hasTextContaining("-Xss2048k")
                .hasTextContaining("-Xms1g")
                .hasTextContaining("-Xmx2g")
                .hasTextContaining("-XX:ActiveProcessorCount=2");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.resources.container.requests.cpu", "10",
                "synchrony.resources.container.requests.memory", "10GB",
                "synchrony.resources.container.limits.cpu", "20",
                "synchrony.resources.container.limits.memory", "20GB"
        ));

        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");

        // verify requests
        assertThat(synchronySts.getContainer("synchrony").getRequests().path("cpu")).hasValueEqualTo(10);
        assertThat(synchronySts.getContainer("synchrony").getRequests().path("memory")).hasTextEqualTo("10GB");

        // verify limits
        assertThat(synchronySts.getContainer("synchrony").getLimits().path("cpu")).hasValueEqualTo(20);
        assertThat(synchronySts.getContainer("synchrony").getLimits().path("memory")).hasTextEqualTo("20GB");
    }
}
