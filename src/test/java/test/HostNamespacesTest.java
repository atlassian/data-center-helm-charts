package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

public class HostNamespacesTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void testHostNamespacesNoneSet(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        final var hostNetwork = resources.getStatefulSet(product.getHelmReleaseName())
                .getPodSpec().path("hostNetwork");
        final var hostPID = resources.getStatefulSet(product.getHelmReleaseName())
                .getPodSpec().path("hostPID");
        assert hostNetwork.isEmpty();
        assert hostPID.isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void testHostNamespacesSet(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "hostNamespaces.hostNetwork", "false",
                "hostNamespaces.hostPID", "false"

        ));
       final var hostNetwork = resources.getStatefulSet(product.getHelmReleaseName())
                .getPodSpec().path("hostNetwork");
       final var hostPID = resources.getStatefulSet(product.getHelmReleaseName())
                .getPodSpec().path("hostPID");
       assert hostNetwork.asText().equals("false");
       assert hostPID.asText().equals("false");
    }
}
