package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Container;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class CustomPortTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket", "bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void custom_port_tomcat_server(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".ports.http", "1234"));

        Container container = resources.getStatefulSet(product.getHelmReleaseName()).getContainer();

        container.getEnv().assertHasValue("ATL_TOMCAT_PORT", "1234");
        assertThat(container.getReadinessProbe().get("httpGet").get("port")).hasValueEqualTo(1234);
    }


    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void custom_port_bitbucket(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".ports.http", "1234"));

        Container container = resources.getStatefulSet(product.getHelmReleaseName()).getContainer();

        container.getEnv().assertHasValue("SERVER_PORT", "1234");
        assertThat(container.getReadinessProbe().get("httpGet").get("port")).hasValueEqualTo(1234);
    }
}
