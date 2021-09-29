package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Deployment;
import test.model.Kind;
import test.model.Product;
import test.model.StatefulSet;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class BambooAgentTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "agent")
    void baseUrlSet(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname+".server", "bamboo.bamboo.svc.cluster.local"
        ));

        final var deployment = resources.getDeployment(product.getHelmReleaseName());
        final var env = deployment.getContainer().getEnv();
        env.assertHasValue("BAMBOO_SERVER", "http://bamboo.bamboo.svc.cluster.local");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "agent")
    void securityTokenSet(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname+".securityToken", "49387ba37d9dcbaca542f21934a0a4e0f28c0350"
        ));

        final var deployment = resources.getDeployment(product.getHelmReleaseName());
        final var env = deployment.getContainer().getEnv();
        env.assertHasValue("SECURITY_TOKEN", "49387ba37d9dcbaca542f21934a0a4e0f28c0350");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "agent")
    void additionalEnvironmentVariables(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname+".additionalEnvironmentVariables[0].name", "MY_ENV_VAR",
                pname+".additionalEnvironmentVariables[0].value", "env-value"
        ));

        final var deployment = resources.getDeployment(product.getHelmReleaseName());
        final var env = deployment.getContainer().getEnv();
        env.assertHasValue("MY_ENV_VAR", "env-value");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "agent")
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
    @EnumSource(value = Product.class, names = "agent")
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
    @EnumSource(value = Product.class, names = "agent")
    void deployment_empty_limits(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        Deployment deployment = resources.getDeployment(product.getHelmReleaseName());

        assertThat(deployment.getContainer(product.name()).getLimits()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "agent")
    void deployment_resource_requests_and_limits(Product product) throws Exception {
        final var pname = product.name().toLowerCase();
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                pname+".resources.container.requests.cpu", "10",
                pname+".resources.container.requests.memory", "10GB",
                pname+".resources.container.limits.cpu", "20",
                pname+".resources.container.limits.memory", "20GB"
        ));

        Deployment deployment = resources.getDeployment(product.getHelmReleaseName());

        // verify requests
        assertThat(deployment.getContainer(product.name()).getRequests().path("cpu")).hasValueEqualTo(10);
        assertThat(deployment.getContainer(product.name()).getRequests().path("memory")).hasTextEqualTo("10GB");

        // verify limits
        assertThat(deployment.getContainer(product.name()).getLimits().path("cpu")).hasValueEqualTo(20);
        assertThat(deployment.getContainer(product.name()).getLimits().path("memory")).hasTextEqualTo("20GB");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "agent")
    void custom_scheduler(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "schedulerName", "second_scheduler"));

        JsonNode podSpec = resources.getDeployment(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("schedulerName")).hasTextEqualTo("second_scheduler");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "agent")
    void default_scheduler(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "schedulerName", ""));

        JsonNode podSpec = resources.getDeployment(product.getHelmReleaseName()).getPodSpec();
        assertThat(podSpec.path("schedulerName")).isEmpty();
    }
}
