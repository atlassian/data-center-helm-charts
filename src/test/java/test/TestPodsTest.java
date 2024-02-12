package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.jackson.JsonNodeAssert.assertThat;

class TestPodsTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_default_annotations(Product product) throws Exception {
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "database.credentials.secretName", "db-secret"
            ));
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            assertThat(pod.getAnnotations()).isObject(Map.of(
                    "helm.sh/hook", "test",
                    "helm.sh/hook-delete-policy", "before-hook-creation,hook-succeeded"
            ));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_annotations(Product product) throws Exception {

        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "database.credentials.secretName", "db-secret",
                    "testPods.annotations.podAnnotation1", "podOfHumpbacks",
                    "testPods.annotations.podAnnotation2", "podOfOrcas"
            ));
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            assertThat(pod.getAnnotations()).isObject(Map.of(
                    "podAnnotation1", "podOfHumpbacks",
                    "podAnnotation2", "podOfOrcas"
            ));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_default_labels(Product product) throws Exception {
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        String helmChartVersion = product.getHelmChartVersion();
        String appVersion = product.getAppVersion();
        for (String testPod : testPods) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "database.credentials.secretName", "db-secret"
            ));
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            assertThat(pod.getMetadata().path("labels")).isObject(Map.of(
                    "helm.sh/chart", product.name() + "-" + helmChartVersion,
                    "app.kubernetes.io/name",product.name(),
                    "app.kubernetes.io/instance", product.getHelmReleaseName(),
                    "app.kubernetes.io/version", appVersion,
                    "app.kubernetes.io/managed-by", "Helm"
            ));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_labels(Product product) throws Exception {
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "database.credentials.secretName", "db-secret",
                    "testPods.labels.label1", "value1",
                    "testPods.labels.label2", "value2"
            ));
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            assertThat(pod.getMetadata().path("labels")).isObject(Map.of(
                    "label1", "value1",
                    "label2", "value2"
            ));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_node_selector(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.credentials.secretName", "db-secret",
                "testPods.nodeSelector.nodename", "special-node"
        ));
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {

            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);

            assertThat(pod.getSpec().path("nodeSelector").path("nodename")).hasTextEqualTo("special-node");
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_scheduler_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.credentials.secretName", "db-secret",
                "testPods.schedulerName", "my-scheduler"));
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            assertEquals("my-scheduler", pod.getSpec().path("schedulerName").asText());
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.credentials.secretName", "db-secret",
                "testPods.resources.requests.cpu", "1",
                "testPods.resources.limits.cpu", "2",
                "testPods.resources.requests.memory", "2Mi",
                "testPods.resources.limits.memory", "3Mi"));
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            assertEquals("1", pod.getSpec().path("containers").path(0).path("resources").path("requests").path("cpu").asText());
            assertEquals("2", pod.getSpec().path("containers").path(0).path("resources").path("limits").path("cpu").asText());
            assertEquals("2Mi", pod.getSpec().path("containers").path(0).path("resources").path("requests").path("memory").asText());
            assertEquals("3Mi", pod.getSpec().path("containers").path(0).path("resources").path("limits").path("memory").asText());
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_images(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "testPods.image.statusTestContainer", "centos",
                "testPods.image.permissionsTestContainer", "centos"));
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test");
        for (String testPod : testPods) {
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            assertEquals("centos", pod.getSpec().path("containers").path(0).path("image").asText());
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_tolerations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.credentials.secretName", "db-secret",
                "testPods.tolerations[0].key", "other-pod",
                "testPods.tolerations[0].operator", "Exists",
                "testPods.tolerations[0].effect", "NoSchedule"));
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            JsonNode tolerations = pod.getSpec().get("tolerations");
            assertThat(tolerations).isArrayWithNumberOfChildren(1);
            assertThat(tolerations.get(0).get("key")).hasTextEqualTo("other-pod");
            assertThat(tolerations.get(0).get("operator")).hasTextEqualTo("Exists");
            assertThat(tolerations.get(0).get("effect")).hasTextContaining("NoSchedule");
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_pods_custom_affinity(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "database.credentials.secretName", "db-secret",
                "testPods.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].key", "kubernetes.io/os",
                "testPods.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].operator", "in",
                "testPods.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].values[0]", "centos"));
        List<String> testPods = List.of("application-status-test", "shared-home-permissions-test", "db-connectivity-test");
        if (product.name().equals("crowd")) {
            testPods = List.of("application-status-test", "shared-home-permissions-test");
        }
        for (String testPod : testPods) {
            final var pod = resources.get(Kind.Pod, Pod.class, product.getHelmReleaseName() + "-" + testPod);
            JsonNode affinity = pod.getSpec().get("affinity");
            assertThat(affinity.path("nodeAffinity").path("requiredDuringSchedulingIgnoredDuringExecution").path("nodeSelectorTerms").get(0).path("matchExpressions").get(0).path("values").get(0)).hasTextEqualTo("centos");
        }
    }
}
