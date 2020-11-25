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
    @EnumSource(Product.class)
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
}
