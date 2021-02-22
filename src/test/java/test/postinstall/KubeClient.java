package test.postinstall;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.collection.Array;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;

final class KubeClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(KubeClient.class);

    private final Lazy<KubernetesClient> clientRef = Lazy.of(DefaultKubernetesClient::new);

    io.vavr.collection.Map<Node, Option<NodeMetrics>> getNodeMetrics(final PodSpec podSpec) {
        return getNodes(podSpec.getNodeSelector())
                .toMap(node -> Tuple.of(node, getNodeMetrics(node)));
    }

    private Option<NodeMetrics> getNodeMetrics(Node node) {
        return Try.of(() -> client().top().nodes().metrics(node.getMetadata().getName()))
                .onFailure(ex -> log.warn("Failed to call K8S Ketrics API: {}", ex.getMessage()))
                .toOption();
    }

    /**
     * @return all k8s custer nodes which are available for pod scheduling, based on the node selector in the
     * StatefulSet spec.
     */
    private Traversable<Node> getNodes(final Map<String, String> nodeSelector) {
        return Array.ofAll(client()
                .nodes()
                .withLabels(nodeSelector)
                .list()
                .getItems());
    }

    @Nullable
    StatefulSet getStatefulSet(String statefulSetName, final String namespaceName) {
        return client()
                .apps().statefulSets()
                .inNamespace(namespaceName)
                .withName(statefulSetName)
                .get();
    }

    @Nullable
    Pod getPod(final String podName, final String namespaceName) {
        return client()
                .pods()
                .inNamespace(namespaceName)
                .withName(podName)
                .get();
    }

    private KubernetesClient client() {
        return clientRef.get();
    }

    @Override
    public void close() {
        if (clientRef.isEvaluated()) {
            clientRef.forEach(KubernetesClient::close);
        }
    }
}
