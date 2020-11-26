package test;

import io.vavr.collection.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static test.jackson.JsonNodeAssert.assertThat;

/**
 * Tests the various permutations of the "persistence" value structure in the Helm charts
 */
class PersistenceTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket", "confluence"})
    void persistence_enabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "persistence.enabled", "true"
        ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        verifyVolumeClaimTemplates(statefulSet);
        verifySharedHomeVolume(product, statefulSet);
    }

    private void verifyVolumeClaimTemplates(test.model.StatefulSet statefulSet) {
        final var volumeClaimTemplates = statefulSet
                .getNode("spec")
                .path("volumeClaimTemplates");

        assertThat(volumeClaimTemplates)
                .describedAs("StatefulSet %s should have a single volumeClaimTempate", statefulSet.getName())
                .isArrayWithNumberOfChildren(1);

        verifyLocalHomeVolumeClaimTemplate(
                volumeClaimTemplates.get(0));
    }

    private void verifyLocalHomeVolumeClaimTemplate(com.fasterxml.jackson.databind.JsonNode localHomeTemplate) {
        assertThat(localHomeTemplate.path("metadata").path("name"))
                .hasTextEqualTo("local-home");
        assertThat(localHomeTemplate.path("spec").path("accessModes"))
                .isArrayWithChildren("ReadWriteOnce");
    }

    private void verifySharedHomeVolume(Product product, test.model.StatefulSet statefulSet) {
        final var volumes = statefulSet.getPodSpec().required("volumes");
        final var sharedHomeVolume = Array.ofAll(volumes)
                .find(volume -> volume.path("name").asText().equals("shared-home"));

        assertThat(sharedHomeVolume)
                .describedAs("StatefulSet %s should have a shared-home volume")
                .isDefined();

        assertThat(sharedHomeVolume.map(node -> node.path("persistentVolumeClaim")))
                .hasValueSatisfying(node -> assertThat(node.path("claimName"))
                        .describedAs("StatefulSet %s should have a shared-home volume with the correct persistentVolumeClaim name")
                        .hasTextEqualTo("%s-shared-home", product));
    }
}
