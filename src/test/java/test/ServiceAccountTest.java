package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.KubeResource;
import test.model.Product;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.ClusterRole;
import static test.model.Kind.ClusterRoleBinding;
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
                        assertThat(statefulSet.getPodSpec().path("serviceAccountName"))
                                .describedAs("StatefulSet %s should have the configured ServiceAccount name", statefulSet.getName())
                                .hasTextEqualTo("foo"));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void serviceAccount_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "serviceAccount.annotations.myAnnotation", "myValue"));

        assertThat(resources.get(ServiceAccount).getAnnotations()).isObject(Map.of("myAnnotation", "myValue"));
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
                        assertThat(statefulSet.getPodSpec().path("serviceAccountName"))
                                .describedAs("StatefulSet %s should have the default ServiceAccount name", statefulSet.getName())
                                .hasTextEqualTo("default"));
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
                        assertThat(statefulSet.getPodSpec().path("serviceAccountName"))
                                .describedAs("StatefulSet %s should have the configured ServiceAccount name", statefulSet.getName())
                                .hasTextEqualTo("foo"));
    }

    @ParameterizedTest
    @EnumSource(Product.class)
    void serviceAccount_imagePullSecrets(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "serviceAccount.imagePullSecrets", "{foo,bar}"));

        assertThat(resources.get(ServiceAccount).getNode("imagePullSecrets"))
                .isArrayWithChildren("foo", "bar");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "bitbucket"})
    void cluster_role_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true",
                "serviceAccount.clusterRole.name", "foo"));

        assertThat(resources.get(ServiceAccount).getName())
                .isEqualTo(product.getHelmReleaseName());
        assertThat(resources.get(ClusterRole).getName())
                .isEqualTo("foo");

        verifyClusterRoleBinding(resources.get(ClusterRoleBinding),
                "foo", "foo", product.getHelmReleaseName());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "bitbucket"})
    void cluster_role_create_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true",
                "serviceAccount.clusterRole.create", "false"));

        assertThat(resources.get(ServiceAccount).getName())
                .isEqualTo(product.getHelmReleaseName());

        assertThat(resources.getAll(ClusterRole))
                .describedAs("No ClusterRole resources should be created")
                .isEmpty();

        verifyClusterRoleBinding(resources.get(ClusterRoleBinding),
                product.getHelmReleaseName(), product.getHelmReleaseName(), product.getHelmReleaseName());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "bitbucket"})
    void cluster_role_binding_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true",
                "serviceAccount.clusterRoleBinding.name", "foo"));

        assertThat(resources.get(ServiceAccount).getName())
                .isEqualTo(product.getHelmReleaseName());
        assertThat(resources.get(ClusterRole).getName())
                .isEqualTo(product.getHelmReleaseName());

        verifyClusterRoleBinding(resources.get(ClusterRoleBinding),
                "foo", product.getHelmReleaseName(), product.getHelmReleaseName());
    }

    private void verifyClusterRoleBinding(final KubeResource clusterRoleBinding,
                                          final String expectedName,
                                          final String expectedRoleName,
                                          final String expectedServiceAccountName) {
        assertThat(clusterRoleBinding.getName())
                .isEqualTo(expectedName);

        assertThat(clusterRoleBinding.getNode("roleRef", "name"))
                .hasTextEqualTo(expectedRoleName);
        assertThat(clusterRoleBinding.getNode("subjects"))
                .isArrayWithNumberOfChildren(1);

        assertThat(clusterRoleBinding.getNode("subjects").get(0))
                .isObject(Map.of(
                        "kind", "ServiceAccount",
                        "name", expectedServiceAccountName));
    }
}
