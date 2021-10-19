# Product scaling
For optimum performance and stability the appropriate resource `requests` and `limits` should be defined for each pod. The number of pods in the product cluster should also be carefully considered. Kubernetes provides means for horizontal and vertical scaling of the deployed pods within a cluster, these approaches are described below.

## Horizontal scaling - adding pods
The Helm charts provision one `StatefulSet` by default. The number of replicas within this `StatefulSet` can be altered either declaratively or imperatively. Note that the Ingress must support cookie-based session affinity in order for the products to work correctly in a multi-node configuration.


=== "Declaratively"
      1. Update `values.yaml` by modifying the `replicaCount` appropriately.
      2. Apply the patch:
      ```shell
      helm upgrade <release> <chart> -f <values file>
      ```

=== "Imperatively"
      ```shell
      kubectl scale statefulsets <statefulsetset-name> --replicas=n
      ```

!!!note "Initial cluster size"
      **Jira**, **Confluence**, and **Crowd** all require manual configuration after the first pod is deployed and before scaling up to additional pods, therefore when you deploy the product only one pod (replica) is created. The initial number of pods that should be started at deployment of each product is set in the `replicaCount` variable found in the values.yaml and should always be kept as 1.

!!!warning "Bamboo cluster size"
      **Bamboo server** currently has [limitations relating to clustering](../../troubleshooting/LIMITATIONS.md#cluster-size), as such, unlike the other products Bamboo server can only be scaled to a maximum of `1` pod.

For details on modifying the `cpu` and `memory` requirements of the `StatefulSet` see section [Vertical Scaling](#vertical-scaling-adding-resources) below. Additional details on the resource requests and limits used by the `StatfulSet` can be found in [Resource requests and limits](REQUESTS_AND_LIMITS.md).

### Scaling Jira safely
At present there are issues relating to index replication with Jira when immediately scaling up by more than 1 pod at a time. See [Jira and horizontal scaling](../../troubleshooting/LIMITATIONS.md#jira-limitations-and-horizontal-scaling).

!!!warning "Before scaling your cluster"

      Make sure there's at least one snapshot file in the `<shared-home>/export/indexsnapshots` directory. New pods will attempt to use the files in this directory to replicate the index. If there is no snapshot present in  `<shared-home>/export/indexsnapshots` then [create an initial index snapshot](JIRA_INDEX_SNAPSHOT.md)

Having followed the steps above, and ensured a healthy snapshot index is available, [scale the cluster as necessary](#horizontal-scaling-adding-pods). Once scaling is complete confirm that the index is still healthy [using the approach prescribed in Step 3](JIRA_INDEX_SNAPSHOT.md). If there are still indexing issues then please refer to the guides below for details on how address them:

* [Unable to perform a background re-index error](https://confluence.atlassian.com/jirakb/how-to-fix-a-jira-application-that-is-unable-to-perform-a-background-re-index-at-this-time-error-316637947.html)
* [Troubleshoot index problems in Jira server](https://confluence.atlassian.com/jirakb/troubleshoot-index-problems-in-jira-server-203394752.html)

## Vertical scaling - adding resources
The resource `requests` and `limits` for a `StatefulSet` can be defined before product deployment or for deployments that are already running within the Kubernetes cluster. Take note that vertical scaling will result in the pod being re-created with the updated values.

### Prior to deployment
Before performing a helm install update the appropriate products `values.yaml` `container` stanza with the desired `requests` and `limits` values i.e. 
```yaml
 container: 
  limits:
    cpu: "4"
    memory: "4G"
  requests:
    cpu: "2"
    memory: "2G"
```

### Post deployment
For existing deployments the `requests` and `limits` values can be dynamically updated either declaratively or imperatively 

=== "Declaratively"
      This the preferred approach as it keeps the state of the cluster, and the helm charts themselves in sync.
      
      1. Update `values.yaml` appropriately
      2. Apply the patch:
      
      ```shell
      helm upgrade <release> <chart> -f <values file>
      ```

=== "Imperatively"
      Using `kubectl edit` on the appropriate `StatefulSet` the respective `cpu` and `memory` values can be modified i.e.

      ```yaml
      resources:
        requests:
          cpu: "2"
          memory: 2G
      ```

      Saving the changes will then result in the existing product pod(s) being re-provisioned with the updated values.
