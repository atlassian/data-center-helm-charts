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

public class RequestsTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void sts_empty_limits(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        StatefulSet productSts = resources.getStatefulSet(product.getHelmReleaseName());

        assertThat(productSts.getContainer(product.name()).getLimits()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void sts_resource_requests_and_limits(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".resources.container.requests.cpu", "10",
                product + ".resources.container.requests.memory", "10GB",
                product + ".resources.container.limits.cpu", "20",
                product + ".resources.container.limits.memory", "20GB"
        ));

        StatefulSet productSts = resources.getStatefulSet(product.getHelmReleaseName());

        // verify requests
        assertThat(productSts.getContainer(product.name()).getRequests().path("cpu")).hasValueEqualTo(10);
        assertThat(productSts.getContainer(product.name()).getRequests().path("memory")).hasTextEqualTo("10GB");

        // verify limits
        assertThat(productSts.getContainer(product.name()).getLimits().path("cpu")).hasValueEqualTo(20);
        assertThat(productSts.getContainer(product.name()).getLimits().path("memory")).hasTextEqualTo("20GB");

        // verify ActiveProcessorCount
        final var additionalJvmArgs = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-jvm-config")
            .getNode("data", "additional_jvm_args");
        assertThat(additionalJvmArgs)
            .hasSeparatedTextContaining("-XX:ActiveProcessorCount=10");

        final var resourcesMilliCpu = helm.captureKubeResourcesFromHelmChart(product, Map.of(
            product + ".resources.container.requests.cpu", "3100m"
        ));
        final var additionalJvmArgsMilliCpu = resourcesMilliCpu
            .get(Kind.ConfigMap, product.getHelmReleaseName() + "-jvm-config").getNode("data", "additional_jvm_args");
        assertThat(additionalJvmArgsMilliCpu)
            .hasSeparatedTextContaining("-XX:ActiveProcessorCount=3");

        final var resourcesBelow1000m = helm.captureKubeResourcesFromHelmChart(product, Map.of(
            product + ".resources.container.requests.cpu", "200m"
        ));
        final var additionalJvmArgsBelow1000m = resourcesBelow1000m
            .get(Kind.ConfigMap, product.getHelmReleaseName() + "-jvm-config").getNode("data", "additional_jvm_args");
        assertThat(additionalJvmArgsBelow1000m)
            .hasSeparatedTextContaining("-XX:ActiveProcessorCount=1");
    }

}
