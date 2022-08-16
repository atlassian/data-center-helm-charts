package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.Product;
import test.model.Service;

import java.util.Map;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static test.jackson.JsonNodeAssert.assertThat;

class BitbucketSshServiceTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, mode = INCLUDE, names = "bitbucket")
    void defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.sshService.enabled", "true"));

        var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName() + "-ssh");

        assertThat(service.getPort("ssh")).hasValueSatisfying(node -> {
            assertThat(node.path("port")).hasValueEqualTo(22);
            assertThat(node.path("targetPort")).hasTextContaining("ssh");
            assertThat(node.path("protocol")).hasTextContaining("TCP");
        });
        assertThat(service.getType()).hasTextEqualTo("LoadBalancer");
        assertThat(service.getMetadata().path("annotations")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, mode = INCLUDE, names = "bitbucket")
    void values_set(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.sshService.enabled", "true",
                "bitbucket.sshService.port", "7999",
                "bitbucket.sshService.host", "hostname",
                "bitbucket.sshService.type", "ClusterIP",
                "bitbucket.sshService.annotations.test" ,"test",
                "bitbucket.sshService.annotations.test\\.property", "test",
                "ingress.host", "hostname"));

        var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName() + "-ssh");

        assertThat(service.getPort("ssh")).hasValueSatisfying(node -> assertThat(node.path("port")).hasValueEqualTo(7999));
        assertThat(service.getType()).hasTextEqualTo("ClusterIP");
        assertThat(service.getMetadata().path("annotations")).isObject(Map.of(
                "test", "test",
                "test.property", "test"));

        resources.getStatefulSet(product.getHelmReleaseName()).getContainer().getEnv().
                assertHasValue("PLUGIN_SSH_BASEURL", "ssh://hostname:7999/");
    }
}
