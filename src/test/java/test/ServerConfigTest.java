package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class ServerConfigTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket", "bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void non_root_tests(Product product) throws Exception {
        for (String[] args : new String[][]{{"true", "false"}, {"false", "true"}}) {
            server_config_volumes(product, args[0], args[1]);
            server_config_volume_mounts(product, args[0], args[1]);
            server_config_config_map(product, args[0], args[1]);
            use_custom_server_xml(product, args[0], args[1]);
            enable_server_xml_config_map_ingress_defaults(product, args[0], args[1]);
            enable_server_xml_config_map_ingress_http(product, args[0], args[1]);
            server_xml_proxy_secure_overrides(product, args[0], args[1]);
        }
    }

    void server_config_volumes(Product product, String generatedByHelm, String restrictedSCC) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", restrictedSCC,
                product.name() + ".tomcatConfig.generateByHelm", generatedByHelm,
                product.name() + ".seraphConfig.generateByHelm", generatedByHelm
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        //assert server.xml volume
        assertThat(statefulSet.getVolume("server-xml").get().path("configMap").get("name")).hasTextEqualTo(product.getHelmReleaseName() + "-server-config");
        assertThat(statefulSet.getVolume("server-xml").get().path("configMap").get("items").path(0).path("key")).hasTextEqualTo("server.xml");
        assertThat(statefulSet.getVolume("server-xml").get().path("configMap").get("items").path(0).path("path")).hasTextEqualTo("server.xml");

        //assert seraph-config.xml volume
        if (!product.name().equals("crowd")) {
            assertThat(statefulSet.getVolume("seraph-config-xml").get().path("configMap").get("name")).hasTextEqualTo(product.getHelmReleaseName() + "-server-config");
            assertThat(statefulSet.getVolume("seraph-config-xml").get().path("configMap").get("items").path(0).path("key")).hasTextEqualTo("seraph-config.xml");
            assertThat(statefulSet.getVolume("seraph-config-xml").get().path("configMap").get("items").path(0).path("path")).hasTextEqualTo("seraph-config.xml");
        }

        if (product.name().equals("bamboo")) {
            assertThat(statefulSet.getVolume("init-properties").get().path("configMap").get("name")).hasTextEqualTo(product.getHelmReleaseName() + "-init-properties");
        }
    }

    void server_config_volume_mounts(Product product, String generatedByHelm, String restrictedSCC) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", restrictedSCC,
                product.name() + ".tomcatConfig.generateByHelm", generatedByHelm,
                product.name() + ".seraphConfig.generateByHelm", generatedByHelm
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert server.xml volumeMount
        String expectedServerXmlPath = String.format("/opt/atlassian/%s/conf/server.xml", product.name());
        if (product.name().equals("crowd")) {
            expectedServerXmlPath = String.format("/opt/atlassian/%s/apache-tomcat/conf/server.xml", product.name());
        }
        assertThat(statefulSet.getContainer().getVolumeMount("server-xml").path("mountPath"))
                .hasTextEqualTo(expectedServerXmlPath);
        assertThat(statefulSet.getContainer().getVolumeMount("server-xml").path("subPath")).hasTextEqualTo("server.xml");

        // assert seraph-config.xml volumeMount
        String productDirectory = product.name();
        if (product.name().contains("jira") || product.name().contains("bamboo")) {
            productDirectory = String.format("atlassian-%s", product);
        }

        if (!product.name().equals("crowd")) {
            String expectedSeraphXmlPath = String.format("/opt/atlassian/%s/%s/WEB-INF/classes/seraph-config.xml",
                    product.name(), productDirectory);
            assertThat(statefulSet.getContainer().getVolumeMount("seraph-config-xml").path("mountPath"))
                    .hasTextEqualTo(expectedSeraphXmlPath);
            assertThat(statefulSet.getContainer().getVolumeMount("seraph-config-xml").path("subPath")).hasTextEqualTo("seraph-config.xml");
        }

        if (product.name().equals("bamboo")) {
            assertThat(statefulSet.getContainer().getVolumeMount("init-properties").path("mountPath"))
                    .hasTextEqualTo("/opt/atlassian/bamboo/atlassian-bamboo/WEB-INF/classes/bamboo-init.properties");
        }
    }

    void server_config_config_map(Product product, String generatedByHelm, String restrictedSCC) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", restrictedSCC,
                product.name() + ".tomcatConfig.generateByHelm", generatedByHelm,
                product.name() + ".seraphConfig.generateByHelm", generatedByHelm,
                product.name() + ".tomcatConfig.port", "1234",
                product.name() + ".tomcatConfig.acceptCount", "5678",
                product.name() + ".tomcatConfig.maxHttpHeaderSize", "9876",
                product.name() + ".tomcatConfig.maxThreads", "1000",
                product.name() + ".tomcatConfig.minSpareThreads", "200",
                product.name() + ".tomcatConfig.connectionTimeout", "30000",
                "ingress.host", String.format("%s.com", product.name())
                ));

        // assert a few server.xml elements in server-config ConfigMap
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("Connector port=\"1234\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("maxDays=\"-1\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("requestAttributesEnabled=\"false\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("acceptCount=\"5678\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("maxHttpHeaderSize=\"9876\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"443\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"" + String.format("%s.com", product.name()));

    }
    void use_custom_server_xml(Product product, String generatedByHelm, String restrictedSCC) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", restrictedSCC,
                product.name() + ".tomcatConfig.generateByHelm", generatedByHelm,
                product.name() + ".seraphConfig.generateByHelm", generatedByHelm,
                product.name() + ".tomcatConfig.customServerXml", "<xml><Server>proxyName=\"example.com\"</Server></xml>"
        ));

        // assert custom server.xml is used in configmap data
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"example.com\"");
    }

    void enable_server_xml_config_map_ingress_defaults(Product product, String generatedByHelm, String restrictedSCC) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", restrictedSCC,
                product.name() + ".tomcatConfig.generateByHelm", generatedByHelm,
                product.name() + ".seraphConfig.generateByHelm", generatedByHelm,
                "ingress.host", String.format("%s.com", product.name())
        ));

        // assert custom server.xml is used in configmap data
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"443\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("secure=\"true\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("scheme=\"https\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"" + String.format("%s.com", product.name()));
    }

    void enable_server_xml_config_map_ingress_http(Product product, String generatedByHelm, String restrictedSCC) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", restrictedSCC,
                product.name() + ".tomcatConfig.generateByHelm", generatedByHelm,
                product.name() + ".seraphConfig.generateByHelm", generatedByHelm,
                "ingress.https", "false",
                "ingress.host", String.format("%s.com", product.name())
        ));

        // assert that ingress related properties are correctly set
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"80\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("secure=\"false\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("scheme=\"http\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"" + String.format("%s.com", product.name()));
    }

    void server_xml_proxy_secure_overrides(Product product, String generatedByHelm, String restrictedSCC) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", restrictedSCC,
                product.name() + ".tomcatConfig.generateByHelm", generatedByHelm,
                product.name() + ".seraphConfig.generateByHelm", generatedByHelm,
                product.name() + ".tomcatConfig.proxyPort", "1234",
                product.name() + ".tomcatConfig.secure", "true",
                product.name() + ".tomcatConfig.scheme", "https",
                product.name() + ".tomcatConfig.proxyName", "foo.bar"
        ));

        // assert that ingress related properties are correctly set
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"1234\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("secure=\"true\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("scheme=\"https\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"foo.bar\"");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void security_context_disabled_with_openshift(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", "true"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getSpec().path("securityContext")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void disable_nfs_perm_fixer_openshift(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", "true"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainers()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void disable_security_context_jmx_openshift(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "openshift.runWithRestrictedSCC", "true",
                "monitoring.exposeJmxMetrics", "true"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainers().path(0).path("securityContext")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "jira"}, mode = EnumSource.Mode.INCLUDE)
    void additional_connector_defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                product.name() + ".tunnel.additionalConnector.port", "8093"
        ));

        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("port=\"8093\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("connectionTimeout=\"20000\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("maxThreads=\"50\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("minSpareThreads=\"10\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("enableLookups=\"false\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("acceptCount=\"10\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("URIEncoding=\"UTF-8\"");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "jira"}, mode = EnumSource.Mode.INCLUDE)
    void additional_connector_overrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                product.name() + ".tunnel.additionalConnector.port", "8093",
                product.name() + ".tunnel.additionalConnector.connectionTimeout", "11",
                product.name() + ".tunnel.additionalConnector.maxThreads", "11",
                product.name() + ".tunnel.additionalConnector.minSpareThreads", "11",
                product.name() + ".tunnel.additionalConnector.enableLookups", "true",
                product.name() + ".tunnel.additionalConnector.acceptCount", "11",
                product.name() + ".tunnel.additionalConnector.secure", "true",
                product.name() + ".tunnel.additionalConnector.URIEncoding", "UTF-9"
        ));

        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("port=\"8093\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("connectionTimeout=\"11\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("maxThreads=\"11\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("minSpareThreads=\"11\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("enableLookups=\"true\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("acceptCount=\"11\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("secure=\"true\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("URIEncoding=\"UTF-9\"");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "jira"}, mode = EnumSource.Mode.INCLUDE)
    void access_log_pattern_default(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true"
        ));
        String expectedPattern;
        if (product == Product.confluence) {
            expectedPattern = "pattern=\"%h %{X-AUSERNAME}o %t &quot;%r&quot; %s %b %D %U %I &quot;%{User-Agent}i&quot;";
        } else expectedPattern = "pattern=\"%a %{jira.request.id}r %{jira.request.username}r %t &quot;%m %U%q %H&quot; %s %b %D &quot;%{Referer}i&quot; &quot;%{User-Agent}i&quot; &quot;%{jira.request.assession.id}r&quot;";

        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining(expectedPattern);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence", "jira"}, mode = EnumSource.Mode.INCLUDE)
    void access_log_pattern_overrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                product.name() + ".tomcatConfig.accessLogPattern", "%%"
        ));
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("%%");
    }
}
