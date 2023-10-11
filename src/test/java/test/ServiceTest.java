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
    void service_default_session_affinity(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.port", "1234",
                product + ".service.type", "NodePort"
        ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName());

        assertThat(service.getSpec().path("sessionAffinity")).hasTextEqualTo("None");
        assertThat(service.getSpec().path("sessionAffinityConfig")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void service_client_ip_session_affinity(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".service.sessionAffinity", "ClientIP",
                product + ".service.sessionAffinityConfig.clientIP.timeoutSeconds", "10"
        ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName());

        assertThat(service.getSpec().path("sessionAffinity")).hasTextEqualTo("ClientIP");
        assertThat(service.getSpec().path("sessionAffinityConfig").path("clientIP").path("timeoutSeconds")).hasValueEqualTo(10);
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

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_service_default_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "confluence.service.annotations.confluence", "qwerty",
                "synchrony.enabled", "true"
        ));

        final var annotations = resources.get(Kind.Service, Service.class, product.getHelmReleaseName() + "-synchrony").getAnnotations();

        assertThat(annotations).isObject(Map.of(
                "confluence", "qwerty"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_service_custom_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.service.annotations.confluence", "qwerty-synchrony",
                "synchrony.enabled", "true"
        ));

        final var annotations = resources.get(Kind.Service, Service.class, product.getHelmReleaseName() + "-synchrony").getAnnotations();

        assertThat(annotations).isObject(Map.of(
                "confluence", "qwerty-synchrony"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket", "confluence"})
    void dedicated_hazelcast_service(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".hazelcastService.enabled", "true",
                product.name() + ".hazelcastService.type", "myType"

        ));

        final var hazelcastService = resources.get(Kind.Service, Service.class, product.getHelmReleaseName() + "-hazelcast");

        assertThat(hazelcastService.getType()).hasTextEqualTo("myType");
        assertThat(hazelcastService.getPort("hazelcast"))
                .hasValueSatisfying(node -> assertThat(node.path("port")).hasValueEqualTo(5701));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName());
        assertThat(service.getPort("hazelcast")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket", "confluence"})
    void hazelcast_one_service(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName());
        assertThat(service.getPort("hazelcast"))
                .hasValueSatisfying(node -> assertThat(node.path("port")).hasValueEqualTo(5701));
    }
}
