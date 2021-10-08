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
    @EnumSource(value = Product.class, names = "bitbucket")
    void bitbucket_clustering_enabled_custom_group(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true",
                product + ".clustering.group.secretName", "hazelcast-group-secret",
                product + ".clustering.group.nameSecretKey", "name-key",
                product + ".clustering.group.passwordSecretKey", "password-key"));

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
                        "hazelcast-group-secret", "name-key")
                .assertHasSecretRef("HAZELCAST_GROUP_PASSWORD",
                        "hazelcast-group-secret", "password-key");
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
                .assertHasFieldRef("EHCACHE_LISTENER_HOSTNAME", "status.podIP")
                .assertHasValue("EHCACHE_LISTENER_PORT", "40001")
                .assertHasValue("EHCACHE_OBJECT_PORT", "40011");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "jira")
    void jira_clustering_enabled_custom_ehcache(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".clustering.enabled", "true",
                product + ".ports.ehcache", "12345",
                product + ".ports.ehcacheobject", "23456"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("CLUSTERED", "true")
                .assertHasValue("EHCACHE_LISTENER_PORT", "12345")
                .assertHasValue("EHCACHE_OBJECT_PORT", "23456");
    }
}
