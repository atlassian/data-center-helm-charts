package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static test.model.Kind.ClusterRole;
import static test.model.Kind.ServiceAccount;

/**
 * Tests the various permutations of the "serviceAccount" value structure in the Helm charts
 */
class ServiceAccountTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void serviceAccount_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "serviceAccount.name", "foo"));

        assertThat(resources.get(ServiceAccount).getName()).isEqualTo("foo");

        resources.getStatefulSets()
                .forEach(statefulSet ->
                        assertThat(statefulSet.getPodSpec()
                                .path("serviceAccountName")
                                .asText())
                                .describedAs("StatefulSet %s should have the configured ServiceAccount name", statefulSet.getName())
                                .isEqualTo("foo"));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void serviceAccount_create_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "serviceAccount.create", "false"));

        assertThat(resources.getAll(ServiceAccount))
                .describedAs("No ServiceAccount resources should be created")
                .isEmpty();

        resources.getStatefulSets()
                .forEach(statefulSet ->
                        assertThat(statefulSet.getPodSpec()
                                .path("serviceAccountName")
                                .asText())
                                .describedAs("StatefulSet %s should have the default ServiceAccount name", statefulSet.getName())
                                .isEqualTo("default"));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void serviceAccount_name_create_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "serviceAccount.create", "false",
                "serviceAccount.name", "foo"));

        assertThat(resources.getAll(ServiceAccount))
                .describedAs("No ServiceAccount resources should be created")
                .isEmpty();

        resources.getStatefulSets()
                .forEach(statefulSet ->
                        assertThat(statefulSet.getPodSpec()
                                .path("serviceAccountName")
                                .asText())
                                .describedAs("StatefulSet %s should have the configured ServiceAccount name", statefulSet.getName())
                                .isEqualTo("foo"));
    }
}
