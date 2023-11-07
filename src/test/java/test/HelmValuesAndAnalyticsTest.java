package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.KubeResource;
import test.model.Product;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static test.jackson.JsonNodeAssert.assertThat;

public class HelmValuesAndAnalyticsTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void support_configmap_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalEnvironmentVariables[0].name", "AWS_TOKEN",
                product.name() + ".additionalEnvironmentVariables[0].values", "qwerty123"
                ));
        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).hasTextContaining("Sanitized by Support Utility");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).hasTextNotContaining("qwerty123");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void support_configmap_created_with_sanitized_envs(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).isNotNull();
        assertThat(additionalConfigMap.getConfigMapData().get("analytics.json")).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void support_configmap_created_with_values_only(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
               "atlassianAnalyticsAndSupport.analytics.enabled", "false"
        ));
        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).isNotNull();
        assertThat(additionalConfigMap.getConfigMapData().get("analytics.json")).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void support_configmap_created_with_analytics_only(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.mountHelmValues.enabled", "false"
        ));

        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).isNull();
        assertThat(additionalConfigMap.getConfigMapData().get("analytics.json")).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void support_configmap_not_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.mountHelmValues.enabled", "false",
                "atlassianAnalyticsAndSupport.analytics.enabled", "false"
        ));

        assertThrows(AssertionError.class, () -> {
            resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void volume_mount_is_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode volumeMount = statefulSet.getContainer(product.name()).getVolumeMount("helm-values");
        assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/opt/atlassian/helm");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void volume_mount_is_not_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.analytics.enabled", "false",
                "atlassianAnalyticsAndSupport.mountHelmValues.enabled", "false"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThrows(AssertionError.class, () -> {
            statefulSet.getContainer(product.name()).getVolumeMount("helm-values");
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void volume_is_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getVolume("helm-values").get().path("configMap").path("name")).hasTextEqualTo(product.getHelmReleaseName() + "-helm-values");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void volume_is_not_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.analytics.enabled", "false",
                "atlassianAnalyticsAndSupport.mountHelmValues.enabled", "false"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThrows(NoSuchElementException.class, () -> {
            statefulSet.getVolume("helm-values").get().path("configMap").path("name");
        });
    }
}
