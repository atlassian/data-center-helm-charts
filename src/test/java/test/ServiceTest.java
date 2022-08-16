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
import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests the various permutations of the "<product>.service" value structure in the Helm charts
 */
class ServiceTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void service_port_type(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.port", "1234",
                product + ".service.type", "NodePort"
        ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName());

        assertThat(service.getType())
                .hasTextEqualTo("NodePort");
        assertThat(service.getPort("http"))
                .hasValueSatisfying(node -> assertThat(node.path("port")).hasValueEqualTo(1234));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void service_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.annotations.testAnnotation1", "test1",
                product + ".service.annotations.testAnnotation1\\.property", "test1.1",
                product + ".service.annotations.testAnnotation2", "test2"
        ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName());

        assertThat(service.getAnnotations()).isObject(Map.of(
                "testAnnotation1", "test1",
                "testAnnotation1.property", "test1.1",
                "testAnnotation2", "test2"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void service_loadbalancer_type(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.type", "LoadBalancer",
                product + ".service.loadBalancerIP", "1.1.1.1"
        ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName());

        assertThat(service.getType())
                .hasTextEqualTo("LoadBalancer");
        assertThat(service.getLoadBalancerIP()).hasTextEqualTo("1.1.1.1");
    }
}
