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
For details on modifying the `cpu` and `memory` requirements of the `StatfuleSet` see section [Vertical Scaling](#vertical-scaling-adding-resources) below. Additional details on the resource requests and limits used by the `StatfulSet` can be found in [Resource requests and limits](REQUESTS_AND_LIMITS.md) page.

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
      Using `kubectl edit` on the appropriate `StatefulSet` the respective `cpu` and `memory` values can be modified. Saving the changes will then result in the existing product pod(s) being re-provisioned with the updated values.
