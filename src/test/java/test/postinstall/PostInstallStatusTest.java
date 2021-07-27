package test.postinstall;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.assertj.core.description.Description;
import org.assertj.core.description.LazyTextDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import test.model.Product;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static test.postinstall.Utils.*;

class PostInstallStatusTest {
    private static final KubeClient client = new KubeClient();

    static boolean isBitbucket() {
        return productIs(Product.bitbucket);
    }

    @Test
    void applicationPodsShouldAllBeRunning() {
        forEachPodOfStatefulSet(pod -> {
            final var podPhase = pod.getStatus().getPhase();

            // First assert that the phase is not "pending", and if so we show a special failure message
            assertThat(podPhase)
                    .describedAs(schedulingFailure(pod))
                    .isNotEqualToIgnoringCase("pending");

            // otherwise assert that the pod is running
            assertThat(podPhase)
                    .describedAs("Pod %s should be running", pod.getMetadata().getName())
                    .isEqualToIgnoringCase("Running");
        });
    }

    private Description schedulingFailure(Pod pod) {
        return new LazyTextDescription(() -> {
            final var podSpec = getStatefulSet()
                    .getSpec()
                    .getTemplate()
                    .getSpec();
            return schedulingFailure(pod.getMetadata().getName(), client.getNodeMetrics(podSpec));
        });
    }

    private String schedulingFailure(final String podName, Map<Node, Option<NodeMetrics>> nodeMetrics) {
        final var resourceSummary = netNodesResourceSummary(nodeMetrics);

        return String.format("Pod %s should be running, but has yet to be scheduled on the cluster. Current node usage is %s",
                podName, resourceSummary);
    }

    @Test
    void applicationPodContainersShouldAllBeReady() {
        forEachPodOfStatefulSet(pod -> {
            final var containerStatuses = Array.ofAll(pod.getStatus().getContainerStatuses());

            assumeThat(containerStatuses)
                    .describedAs("No container statuses present in pod %s", pod.getMetadata().getName())
                    .isNotEmpty();

            final var containerNames = containerStatuses.map(ContainerStatus::getName).mkCharSeq("[", ",", "]");

            assertThat(containerStatuses)
                    .extracting(ContainerStatus::getReady)
                    .describedAs("Containers %s of pod %s should all be ready", containerNames, pod.getMetadata().getName())
                    .containsOnly(true);
        });
    }

    @Test
    @EnabledIf("isBitbucket")
    void elasticSearchIsRunning() {
        var esSetName = getRelease() + "-" + "elasticsearch-master";
        forEachPodOfStatefulSet(esSetName, pod -> {
            final var podPhase = pod.getStatus().getPhase();

            // First assert that the phase is not "pending", and if so we show a special failure message
            assertThat(podPhase)
                    .describedAs(schedulingFailure(pod))
                    .isNotEqualToIgnoringCase("pending");

            // otherwise assert that the pod is running
            assertThat(podPhase)
                    .describedAs("Pod %s should be running", pod.getMetadata().getName())
                    .isEqualToIgnoringCase("Running");
        });

    }

    private void forEachPodOfStatefulSet(Consumer<Pod> consumer) {
        forEachPodOfStatefulSet(getRelease(), consumer);
    }

    private void forEachPodOfStatefulSet(String statefulSetName, Consumer<Pod> consumer) {
        final var statefulSet = getStatefulSet(statefulSetName);
        final var replicaCount = statefulSet.getStatus().getReplicas();

        for (var idx = 0; idx < replicaCount; idx++) {
            consumer.accept(client.getPod(statefulSet.getMetadata().getName() + "-" + idx, getNS()));
        }
    }

    StatefulSet getStatefulSet() {
        return getStatefulSet(getRelease());
    }

    StatefulSet getStatefulSet(String statefulSetName) {
        final var statefulSet = client.getStatefulSet(statefulSetName, getNS());

        assertThat(statefulSet)
                .describedAs("StatefulSet %s not found", statefulSetName)
                .isNotNull();
        return statefulSet;
    }

    @BeforeAll
    static void configure() throws Exception {
        assumeThat(getRelease())
                .describedAs("Cannot run test without Helm release name")
                .isNotEmpty();
        assumeThat(getNS())
                .describedAs("Cannot run test without namespace name")
                .isNotEmpty();
    }

    @AfterAll
    static void disposeOfClient() {
        client.close();
    }
}
