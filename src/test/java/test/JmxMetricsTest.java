package test;

import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.Product;
import test.model.Service;
import test.model.StatefulSet;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.Secret;
import static test.model.Kind.ServiceMonitor;

class JmxMetricsTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_init_container(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true"
        ));

        String sharedHomePath = "/var/atlassian/application-data/shared-home";
        if (product.name().equals("crowd")) {
            sharedHomePath= "/var/atlassian/application-data/crowd/shared";
        }

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("image")).hasTextEqualTo("bitnami/jmx-exporter:0.18.0");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("command").get(0)).hasTextEqualTo("cp");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(0)).hasTextEqualTo("/opt/bitnami/jmx-exporter/jmx_prometheus_javaagent.jar");

        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(1)).hasTextEqualTo(sharedHomePath);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("volumeMounts").get(0).path("mountPath")).hasTextEqualTo(sharedHomePath);

        // assert jmx port
        assertThat(statefulSet.getContainer(product.name()).getPort("jmx").path("containerPort")).hasValueEqualTo(9999);
        assertThat(statefulSet.getContainer(product.name()).getPort("jmx").path("protocol")).hasTextEqualTo("TCP");

        // assert no resources are set
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("resources")).isEmpty();

        // assert jvm configmap has javaagent
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:"+sharedHomePath+"/jmx_prometheus_javaagent.jar=9999:/opt/atlassian/jmx/jmx-config.yaml");

        // assert jmx configmap created and has expected config
        final var jmxConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jmx-config").getDataByKey("jmx-config.yaml");
        assertThat(jmxConfigMap).hasTextContaining("- pattern: ");

        if (product.name().equals("bitbucket")) {
            // assert jmx env var
            statefulSet.getContainer().getEnv().assertHasValue("JMX_ENABLED", "true");
        }
    }


    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_init_container_run_as_root(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true"
        ));
        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("securityContext").path("runAsUser")).hasValueEqualTo(0);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void jmx_init_container_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterInitContainer.resources.requests.cpu", "1",
                "monitoring.jmxExporterInitContainer.resources.requests.memory", "2Gi",
                "monitoring.jmxExporterInitContainer.resources.limits.cpu", "2",
                "monitoring.jmxExporterInitContainer.resources.limits.memory", "3Gi"));

        final var jmxContainerResources = resources.getStatefulSet(product.getHelmReleaseName()).getInitContainers().get(1);

        assertThat(jmxContainerResources.get("resources").get("requests").get("cpu")).hasValueEqualTo(1);
        assertThat(jmxContainerResources.get("resources").get("requests").get("memory")).hasTextEqualTo("2Gi");
        assertThat(jmxContainerResources.get("resources").get("limits").get("cpu")).hasValueEqualTo(2);
        assertThat(jmxContainerResources.get("resources").get("limits").get("memory")).hasTextEqualTo("3Gi");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_init_container_no_root(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterInitContainer.runAsRoot", "false"
        ));
        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("securityContext")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_init_container_custom_security_context(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterInitContainer.runAsRoot", "false",
                "monitoring.jmxExporterInitContainer.customSecurityContext.fsGroup", "1009",
                "monitoring.jmxExporterInitContainer.customSecurityContext.runAsUser", "true"
        ));
        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("securityContext").path("fsGroup")).hasValueEqualTo(1009);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("securityContext").path("runAsUser")).hasToString("true");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_custom_vol_paths(Product product) throws Exception {
        String sharedHomePath = "/var/atlassian/application-data/custom-shared-home";
        if (product.name().equals("crowd")) {
            sharedHomePath= "/var/atlassian/application-data/crowd/custom-shared";
        }
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "volumes.sharedHome.subPath", product.name(),
                "volumes.sharedHome.mountPath", sharedHomePath

        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(1)).hasTextEqualTo(sharedHomePath);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("volumeMounts").get(0).path("mountPath")).hasTextEqualTo(sharedHomePath);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("volumeMounts").get(0).path("subPath")).hasTextEqualTo(product.name());
        // assert jvm configmap has javaagent
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:"+ sharedHomePath + "/jmx_prometheus_javaagent.jar=9999:/opt/atlassian/jmx/jmx-config.yaml");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_custom_init_container(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterImageRepo", "myregistry/myrepo",
                "monitoring.jmxExporterImageTag", "0.17.0"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("image")).hasTextEqualTo("myregistry/myrepo:0.17.0");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_custom_jar(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.fetchJmxExporterJar", "false",
                "monitoring.jmxExporterCustomJarLocation", "/tmp/custom.jar",
                "monitoring.jmxExporterPort", "9000",
                "volumes.sharedHome.persistentVolumeClaim.create", "true"

        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container does not exist
        assertThat(statefulSet.getInitContainers().path(0).path("name")).hasTextNotContaining("fetch-jmx-exporter");

        // assert jvm configmap has custom javaagent path and port
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:/tmp/custom.jar=9000:/opt/atlassian/jmx/jmx-config.yaml");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, mode = EnumSource.Mode.EXCLUDE, names = {"bamboo_agent"})
    void expose_jmx_metrics_enabled_custom_config(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterCustomConfig.rules[0].name", "custom-rule",
                "monitoring.jmxExporterCustomConfig.rules[0].pattern", "^abc"
        ));

        // assert jmx configmap has custom config
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jmx-config").getDataByKey("jmx-config.yaml");
        assertThat(jmvConfigMap).hasTextContaining("rules:");
        assertThat(jmvConfigMap).hasTextContaining("- name: custom-rule");
        assertThat(jmvConfigMap).hasTextContaining("pattern: ^abc");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void jmx_service_test(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterPort", "9000",
                "monitoring.jmxExporterPortType", "NodePort",
                "monitoring.jmxServiceAnnotations.foo", "bar"
        ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName()+"-jmx");

        assertThat(service.getType())
                .hasTextEqualTo("NodePort");
        VavrAssertions.assertThat(service.getPort("jmx"))
                .hasValueSatisfying(node -> assertThat(node.path("port")).hasValueEqualTo(9000));
        assertThat(service.getAnnotations()).isObject(Map.of(
                "foo", "bar"
        ));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void expose_jmx_metrics_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                product+".mesh.enabled", "true"
        ));

        String localHomePath = "/var/atlassian/application-data/mesh";
        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("image")).hasTextEqualTo("bitnami/jmx-exporter:0.18.0");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("command").get(0)).hasTextEqualTo("cp");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(0)).hasTextEqualTo("/opt/bitnami/jmx-exporter/jmx_prometheus_javaagent.jar");

        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(1)).hasTextEqualTo(localHomePath);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("volumeMounts").get(0).path("mountPath")).hasTextEqualTo(localHomePath);

        // assert jmx port
        assertThat(statefulSet.getContainer(product.name()+"-mesh").getPort("jmx").path("containerPort")).hasValueEqualTo(9999);
        assertThat(statefulSet.getContainer(product.name()+"-mesh").getPort("jmx").path("protocol")).hasTextEqualTo("TCP");

        // assert jmx env var
        statefulSet.getContainer().getEnv().assertHasValue("JMX_ENABLED", "true");

        // assert jvm configmap has javaagent
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config-mesh").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:"+localHomePath+"/jmx_prometheus_javaagent.jar=9999:/opt/atlassian/jmx/jmx-config.yaml");

        // assert jmx configmap created and has expected config
        final var jmxConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jmx-config").getDataByKey("jmx-config.yaml");
        assertThat(jmxConfigMap).hasTextContaining("- pattern: ");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void expose_jmx_metrics_enabled_bitbucket_mirror(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                product.name() + ".applicationMode", "mirror"
        ));

        String sharedHomePath = "/var/atlassian/application-data/shared-home";

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("image")).hasTextEqualTo("bitnami/jmx-exporter:0.18.0");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("command").get(0)).hasTextEqualTo("cp");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(0)).hasTextEqualTo("/opt/bitnami/jmx-exporter/jmx_prometheus_javaagent.jar");

        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(1)).hasTextEqualTo(sharedHomePath);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("volumeMounts").get(0).path("mountPath")).hasTextEqualTo(sharedHomePath);

        // assert jmx port
        assertThat(statefulSet.getContainer(product.name()).getPort("jmx").path("containerPort")).hasValueEqualTo(9999);
        assertThat(statefulSet.getContainer(product.name()).getPort("jmx").path("protocol")).hasTextEqualTo("TCP");

        // assert jvm configmap has javaagent
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:"+sharedHomePath+"/jmx_prometheus_javaagent.jar=9999:/opt/atlassian/jmx/jmx-config.yaml");

        // assert jmx configmap created and has expected config
        final var jmxConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jmx-config").getDataByKey("jmx-config.yaml");
        assertThat(jmxConfigMap).hasTextContaining("- pattern: ");

        // assert shared-home volume mount and volume are defined even though not explicitly set in volumes.SharedHome

        assertThat(statefulSet.getContainer("bitbucket").getVolumeMount("shared-home").path("mountPath")).hasTextEqualTo(sharedHomePath);

        assertThat(statefulSet.getVolumes().get(1).path("name")).hasTextEqualTo("shared-home");
        assertThat(statefulSet.getVolumes().get(1).path("emptyDir")).isNotNull();

        statefulSet.getContainer().getEnv().assertHasValue("JMX_ENABLED", "true");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void service_monitor_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.serviceMonitor.create", "true",
                product + ".mesh.enabled", "true",
                product + ".mesh.replicaCount", "3"
        ));

        for (int i = 0; i < 3; i++) {
            String meshReplicaName = product.getHelmReleaseName() + "-mesh-" + i + "-service-monitor";
            resources.assertContains(ServiceMonitor, meshReplicaName);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void service_monitor_enabled_with_custom_values(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.serviceMonitor.create", "true",
                "monitoring.serviceMonitor.prometheusLabelSelector.release", "myprometheus"
        ));

        resources.assertContains(ServiceMonitor, product.getHelmReleaseName() + "-service-monitor");

        final var serviceMonitorSpec = resources.get(ServiceMonitor).getSpec();
        assertThat(serviceMonitorSpec.path("endpoints").path(0).path("interval")).hasTextEqualTo("30s");

        final var serviceMonitorMetadata = resources.get(ServiceMonitor).getMetadata();
        assertThat(serviceMonitorMetadata.path("labels").path("release")).hasTextEqualTo("myprometheus");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void service_monitor_enabled_(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.serviceMonitor.create", "true"
        ));

        resources.assertContains(ServiceMonitor, product.getHelmReleaseName() + "-service-monitor");
        final var serviceMonitorSpec = resources.get(ServiceMonitor).getSpec();
        assertThat(serviceMonitorSpec.path("endpoints").path(0).path("interval")).hasTextEqualTo("30s");
        assertThat(serviceMonitorSpec.path("endpoints").path(0).path("path")).hasTextEqualTo("/metrics");
        assertThat(serviceMonitorSpec.path("endpoints").path(0).path("port")).hasTextEqualTo("jmx");
        assertThat(serviceMonitorSpec.path("endpoints").path(0).path("scheme")).hasTextEqualTo("http");
    }
}
