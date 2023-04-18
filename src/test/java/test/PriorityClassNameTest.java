package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Deployment;
import test.model.Product;
import test.model.StatefulSet;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityClassNameTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void priority_class_names(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "priorityClassName", "high"
                ));

        StatefulSet dcProduct = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode priorityClassName = dcProduct.getPodSpec().get("priorityClassName");
        assertEquals("high", priorityClassName.asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.INCLUDE)
    void priority_class_names_bamboo_agent(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "priorityClassName", "high"
        ));

        Deployment bambooAgent = resources.getDeployment(product.getHelmReleaseName());
        JsonNode priorityClassName = bambooAgent.getPodSpec().get("priorityClassName");
        assertEquals("high", priorityClassName.asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void priority_class_names_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "priorityClassName", "high",
                "bitbucket.mesh.enabled", "true",
                "bitbucket.mesh.priorityClassName", "high"
        ));

        StatefulSet bitbucketMeshSts = resources.getStatefulSet("unittest-bitbucket-mesh");
        JsonNode priorityClassName = bitbucketMeshSts.getPodSpec().get("priorityClassName");
        assertEquals("high", priorityClassName.asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void priority_class_names_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "priorityClassName", "high",
                "synchrony.enabled", "true"
        ));

        StatefulSet synchronySts = resources.getStatefulSet("unittest-confluence-synchrony");
        JsonNode priorityClassName = synchronySts.getPodSpec().get("priorityClassName");
        assertEquals("high", priorityClassName.asText());
    }
}
