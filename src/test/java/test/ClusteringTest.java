package test;

import org.assertj.core.internal.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.*;

class ClusteringTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucket_clustering_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true"));

        resources.assertContains(ClusterRole, product.getHelmReleaseName())
                .assertContains(ClusterRoleBinding, product.getHelmReleaseName());

        test.model.StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        Container container = statefulSet.getContainer();
        Env env = container.getEnv();
        env
                .assertHasFieldRef("KUBERNETES_NAMESPACE", "metadata.namespace")
                .assertHasValue("HAZELCAST_KUBERNETES_SERVICE_NAME", product.getHelmReleaseName())
                .assertHasValue("HAZELCAST_NETWORK_KUBERNETES", "true")
                .assertHasValue("HAZELCAST_PORT", "5701")
                .assertHasSecretRef("HAZELCAST_GROUP_NAME",
                        product.getHelmReleaseName() + "-clustering", "name")
                .assertHasSecretRef("HAZELCAST_GROUP_PASSWORD",
                        product.getHelmReleaseName() + "-clustering", "password");
        resources.assertContains(Secret, product.getHelmReleaseName() + "-clustering");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void confluence_clustering_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true"));

        resources.assertContains(ClusterRole, product.getHelmReleaseName())
                .assertContains(ClusterRoleBinding, product.getHelmReleaseName());

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasFieldRef("KUBERNETES_NAMESPACE", "metadata.namespace")
                .assertHasValue("HAZELCAST_KUBERNETES_SERVICE_NAME", product.getHelmReleaseName())
                .assertHasValue("ATL_CLUSTER_TYPE", "kubernetes")
                .assertHasValue("ATL_CLUSTER_NAME", product.getHelmReleaseName());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_clustering_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("CLUSTERED", "true")
                .assertHasFieldRef("JIRA_NODE_ID", "metadata.name")
                .assertHasFieldRef("EHCACHE_LISTENER_HOSTNAME", "status.podIP");
    }
}
