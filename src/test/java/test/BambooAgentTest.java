package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Deployment;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class BambooAgentTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void baseUrlSet(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.server", "bamboo.bamboo.svc.cluster.local"
        ));

        final var deployment = resources.getDeployment(product.getHelmReleaseName());
        final var env = deployment.getContainer().getEnv();
        env.assertHasValue("BAMBOO_SERVER", "http://bamboo.bamboo.svc.cluster.local");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void bamboo_agent_security_token_secret_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.securityToken.secretName", "security_token_secret",
                "agent.securityToken.secretKey", "ad447ae88f8a6b1da6c78e1510bf44331ed1f956"));

        resources.getDeployment(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasSecretRef("SECURITY_TOKEN", "security_token_secret", "ad447ae88f8a6b1da6c78e1510bf44331ed1f956");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void additionalEnvironmentVariables(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.additionalEnvironmentVariables[0].name", "MY_ENV_VAR",
                "agent.additionalEnvironmentVariables[0].value", "env-value"
        ));

        final var deployment = resources.getDeployment(product.getHelmReleaseName());
        final var env = deployment.getContainer().getEnv();
        env.assertHasValue("MY_ENV_VAR", "env-value");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void additionalInitContainers(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalInitContainers[0].name", "my_init_container",
                "additionalInitContainers[0].image", "my_init_image"
        ));

        final var deployment = resources.getDeployment(product.getHelmReleaseName());
        final var icontainer = deployment.getInitContainer("my_init_container").get();
        assertThat(icontainer.path("image")).hasTextEqualTo("my_init_image");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void additionalContainers(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "additionalContainers[0].name", "my_container",
                "additionalContainers[0].image", "my_image"
        ));

        final var deployment = resources.getDeployment(product.getHelmReleaseName());
        final var icontainer = deployment.getContainer("my_container");
        assertThat(icontainer.get("image")).hasTextEqualTo("my_image");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void deployment_empty_limits(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        Deployment deployment = resources.getDeployment(product.getHelmReleaseName());

        assertThat(deployment.getContainer(product.toString()).getLimits()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void deployment_resource_requests_and_limits(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.resources.container.requests.cpu", "10",
                "agent.resources.container.requests.memory", "10GB",
                "agent.resources.container.limits.cpu", "20",
                "agent.resources.container.limits.memory", "20GB"
        ));

        Deployment deployment = resources.getDeployment(product.getHelmReleaseName());

        // verify requests
        assertThat(deployment.getContainer(product.toString()).getRequests().path("cpu")).hasValueEqualTo(10);
        assertThat(deployment.getContainer(product.toString()).getRequests().path("memory")).hasTextEqualTo("10GB");

        // verify limits
        assertThat(deployment.getContainer(product.toString()).getLimits().path("cpu")).hasValueEqualTo(20);
        assertThat(deployment.getContainer(product.toString()).getLimits().path("memory")).hasTextEqualTo("20GB");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void custom_scheduler(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "schedulerName", "second_scheduler"));

        JsonNode podSpec = resources.getDeployment(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("schedulerName")).hasTextEqualTo("second_scheduler");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "bamboo_agent")
    void default_scheduler(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "schedulerName", ""));

        JsonNode podSpec = resources.getDeployment(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("schedulerName")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"})
    void test_pod_security_context(Product product) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.securityContext.fsGroup", "1000",
                "agent.securityContext.runAsGroup", "1000"));

        JsonNode podSpec = resources.getDeployment(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("securityContext").path("fsGroup")).hasValueEqualTo(1000);
        assertThat(podSpec.path("securityContext").path("runAsGroup")).hasValueEqualTo(1000);

    }

    @ParameterizedTest
    @CsvSource({
            "bamboo_agent,2005"
    })
    void test_pod_security_context_without_fsGroup(Product product, int fsGroup) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.securityContext.runAsGroup", "1000"));

        JsonNode podSpec = resources.getDeployment(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("securityContext").path("fsGroup")).hasValueEqualTo(fsGroup);
        assertThat(podSpec.path("securityContext").path("runAsGroup")).hasValueEqualTo(1000);

    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"})
    void test_container_security_context(Product product) throws Exception {

        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.containerSecurityContext.runAsGroup", "2000"));

        JsonNode containerSecurityContext = resources.getDeployment(product.getHelmReleaseName())
                .getContainer()
                .getSecurityContext();
        assertThat(containerSecurityContext.path("runAsGroup")).hasValueEqualTo(2000);
    }
}
