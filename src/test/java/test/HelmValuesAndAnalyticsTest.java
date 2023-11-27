package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.AnalyticsData;
import test.model.Kind;
import test.model.KubeResource;
import test.model.Product;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static test.jackson.JsonNodeAssert.assertThat;

public class HelmValuesAndAnalyticsTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void support_configmap_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).isNotNull();
        assertThat(additionalConfigMap.getConfigMapData().get("analytics.json")).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void support_configmap_created_with_sanitized_envs(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalEnvironmentVariables[0].name", "AWS_TOKEN",
                product.name() + ".additionalEnvironmentVariables[0].value", "qwerty123"
                ));
        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).hasTextContaining("Sanitized by Support Utility");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).hasTextNotContaining("qwerty123");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void support_configmap_created_with_values_only(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
               "atlassianAnalyticsAndSupport.analytics.enabled", "false"
        ));
        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).isNotNull();
        assertThat(additionalConfigMap.getConfigMapData().get("analytics.json")).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void support_configmap_created_with_analytics_only(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.helmValues.enabled", "false"
        ));

        KubeResource additionalConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        assertThat(additionalConfigMap.getConfigMapData().get("values.yaml")).isNull();
        assertThat(additionalConfigMap.getConfigMapData().get("analytics.json")).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void support_configmap_not_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.helmValues.enabled", "false",
                "atlassianAnalyticsAndSupport.analytics.enabled", "false"
        ));

        assertThrows(AssertionError.class, () -> {
            resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values");
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void volume_mount_is_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode volumeMount = statefulSet.getContainer(product.name()).getVolumeMount("helm-values");
        assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/opt/atlassian/helm");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void volume_mount_is_not_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.analytics.enabled", "false",
                "atlassianAnalyticsAndSupport.helmValues.enabled", "false"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThrows(AssertionError.class, () -> {
            statefulSet.getContainer(product.name()).getVolumeMount("helm-values");
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void volume_is_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getVolume("helm-values").get().path("configMap").path("name")).hasTextEqualTo(product.getHelmReleaseName() + "-helm-values");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void volume_is_not_created(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "atlassianAnalyticsAndSupport.analytics.enabled", "false",
                "atlassianAnalyticsAndSupport.helmValues.enabled", "false"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThrows(NoSuchElementException.class, () -> {
            statefulSet.getVolume("helm-values").get().path("configMap").path("name");
        });
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
        ObjectMapper objectMapper = new ObjectMapper();
        AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
        assertNotNull(analyticsData.getImageTag());
        assertEquals(1, analyticsData.getReplicas());
        assertFalse(analyticsData.isIngressEnabled());
        assertEquals("UNKNOWN", analyticsData.getDbType());
        assertEquals("CLUSTERIP", analyticsData.getServiceType());
        assertFalse(analyticsData.isGrafanaDashboardsCreated());
        assertFalse(analyticsData.isServiceMonitorCreated());
        assertFalse(analyticsData.isJmxEnabled());
        assertFalse(analyticsData.isSharedHomePVCCreated());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_booleans(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "ingress.create", "true",
                "monitoring.serviceMonitor.create", "true",
                "monitoring.grafana.createDashboards", "true",
                "monitoring.exposeJmxMetrics", "true",
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
        ));
        String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
        ObjectMapper objectMapper = new ObjectMapper();
        AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
        assertTrue(analyticsData.isIngressEnabled());
        assertTrue(analyticsData.isIngressNginx());
        assertTrue(analyticsData.isGrafanaDashboardsCreated());
        assertTrue(analyticsData.isServiceMonitorCreated());
        assertTrue(analyticsData.isJmxEnabled());
        assertTrue(analyticsData.isSharedHomePVCCreated());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent", "crowd"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_db_type(Product product) throws Exception {
        Map<String, String> databaseConfigurations;
        if (product == Product.bitbucket) {
            databaseConfigurations = Map.of(
                    "postgres72", "org.postgresql.Driver",
                    "sqlserver11", "com.microsoft.sqlserver.jdbc.SQLSDriver",
                    "oracle10", "oracle.jdbc.driver.OracleDriver",
                    "mysql7", "com.mysql.cj.jdbc.Driver"
            );
        } else {
            databaseConfigurations = Map.of(
                    "postgres72", "org.postgresql.Driver",
                    "mssql01", "com.microsoft.mssql.jdbc.mssql",
                    "oracle10", "oracle.jdbc.driver.OracleDriver",
                    "mysql7", "com.mysql.cj.jdbc.Driver"
            );
        }
        List<String> normalizedDatabaseTypes = List.of("POSTGRES", "MSSQL", "ORACLE", "MYSQL");
        for (Map.Entry<String, String> entry : databaseConfigurations.entrySet()) {
            String databaseType = entry.getKey();
            String databaseDriver = entry.getValue();

            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "database.type", databaseType,
                    "database.driver", databaseDriver
            ));
            String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
            ObjectMapper objectMapper = new ObjectMapper();
            AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
            assertTrue(normalizedDatabaseTypes.contains(analyticsData.getDbType()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent", "crowd"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_db_unknown(Product product) throws Exception {
        List<String> unknownDatabaseTypes = List.of("mydb", "ourdb", "notknown");
        for (String unknownDbType : unknownDatabaseTypes) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    "database.type", unknownDbType,
                    "database.driver", unknownDbType
            ));
            String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
            ObjectMapper objectMapper = new ObjectMapper();
            AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
            assertEquals("UNKNOWN", analyticsData.getDbType());
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent", "crowd"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_known_service_types(Product product) throws Exception {
        List<String> knownServiceTypes = List.of("ClusterIP", "LoadBalancer", "NodePort", "ExternalName");
        for (String svc : knownServiceTypes) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    product.name() + ".service.type", svc
            ));
            String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
            ObjectMapper objectMapper = new ObjectMapper();
            AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
            assertEquals(svc.toUpperCase(), analyticsData.getServiceType());
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_unknown_service_types(Product product) throws Exception {
        List<String> unknownServiceTypes = List.of("ClusterPort", "LoadName", "NodeIp", "ExternalBalancer");
        for (String svc : unknownServiceTypes) {
            final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                    product.name() + ".service.type", svc
            ));
            String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
            ObjectMapper objectMapper = new ObjectMapper();
            AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
            assertEquals("UNKNOWN", analyticsData.getServiceType());
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_image_tag(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.tag", "007"
        ));
        String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
        ObjectMapper objectMapper = new ObjectMapper();
        AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
        assertEquals("007", analyticsData.getImageTag());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void analytics_json_assert_sanitized_image_tag(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "image.tag", "9.1.3-ubuntu"
        ));
        String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
        ObjectMapper objectMapper = new ObjectMapper();
        AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
        assertEquals("9.1.3", analyticsData.getImageTag());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void analytics_json_assert_s3_confluence(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "confluence.s3AttachmentsStorage.bucketName", "mybucket",
                "confluence.s3AttachmentsStorage.bucketRegion", "us-east-1"
        ));
        String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
        ObjectMapper objectMapper = new ObjectMapper();
        AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
        assertTrue(analyticsData.isS3AttachmentsStorageEnabled());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"}, mode = EnumSource.Mode.INCLUDE)
    void analytics_json_assert_s3_jira(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "jira.s3Storage.avatars.bucketName", "mybucket",
                "jira.s3Storage.avatars.bucketRegion", "us-east-1"
        ));
        String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
        ObjectMapper objectMapper = new ObjectMapper();
        AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
        assertTrue(analyticsData.isS3AvatarsEnabled());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void analytics_json_assert_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.mesh.enabled", "true"
        ));
        String analyticsJson = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-helm-values").getConfigMapData().get("analytics.json").asText();
        ObjectMapper objectMapper = new ObjectMapper();
        AnalyticsData analyticsData = objectMapper.readValue(analyticsJson, AnalyticsData.class);
        assertTrue(analyticsData.isBitbucketMeshEnabled());
    }
}
