package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import test.helm.Helm;
import test.model.Product;
import test.model.StatefulSet;
import test.model.Synchrony;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Synchrony.synchronyStatefulSetName;

public class AdditionalLibrariesTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @Test
    void additional_libraries_synchrony() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.confluence, Map.of(
                "synchrony.enabled","true",
                "synchrony.additionalLibraries[0].volumeName","my-custom-volume",
                "synchrony.additionalLibraries[0].subDirectory","mylib",
                "synchrony.additionalLibraries[0].fileName","mysql-connector-java-8.0.25.jar"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(synchronyStatefulSetName());
        JsonNode volumeMount = statefulSet.getContainer("synchrony").getVolumeMount("my-custom-volume");

        assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/opt/atlassian/confluence/confluence/WEB-INF/lib/mysql-connector-java-8.0.25.jar");
        assertThat(volumeMount.path("subPath")).hasTextEqualTo("mylib/mysql-connector-java-8.0.25.jar");
    }

    @Test
    void additional_libraries_synchrony_withoutSubDirectory() throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.confluence, Map.of(
                "synchrony.enabled","true",
                "synchrony.additionalLibraries[0].volumeName","my-custom-volume",
                "synchrony.additionalLibraries[0].fileName","mysql-connector-java-8.0.25.jar"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(synchronyStatefulSetName());
        JsonNode volumeMount = statefulSet.getContainer("synchrony").getVolumeMount("my-custom-volume");

        assertThat(volumeMount.path("mountPath")).hasTextEqualTo("/opt/atlassian/confluence/confluence/WEB-INF/lib/mysql-connector-java-8.0.25.jar");
        assertThat(volumeMount.path("subPath")).hasTextEqualTo("mysql-connector-java-8.0.25.jar");
    }

}
