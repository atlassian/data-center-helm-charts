# Scaling The Products
To run the product in high performance and stable in Kubernetes, the infrastructure should configure properly. This will 
happen by combination of defining the resource request for each pod the number of pods in the product cluster. 

The default values in the provided helm chart is designed to handle small datasets. 
To keep the performance steady the product should scale up based on size of dataset. 
Here is [Data Center infrastructure recommendations](https://confluence.atlassian.com/enterprise/data-center-infrastructure-recommendations-972333478.html) 
 for enterprise size of products on AWS. This can give an idea for scaling the infrastructure in Kubernetes. 
 
Scaling product can be done by adding the number of pods that run the product within the cluster(horizontal scaling) 
and/or increasing the resources of each pod that run the product(vertical scaling).
   

## Horizontal Scaling
The Helm charts provision one `StatefulSet` by default. In order to horizontally scale up or down the cluster `kubectl scale` 
can be used at runtime to provision a multi-node Data Center cluster, with no further configuration required (although 
note that the Ingress must support cookie-based session affinity in order for the 
products to work correctly in a multi-node configuration). Here is the syntax for scaling up/down the Data Center cluster:
```
kubectl scale statefulsets <statefulsetset-name> --replicas=n
```
For details on the `cpu` and `memory` requirements of the product `StatfulSets` see [Resource requests and limits](#resource-requests-and-limits) 

##Vertical Scaling
Vertical Scaling is possible by changing the attributed resources (cpu and memory request/limit) of each node in the cluster. 
Notice scaling up/down a living pod will result by terminating the existed pod and replace by a new pod with the scaled 
resources. 

### Resource requests and limits
To ensure that Kubernetes appropriately schedules resources, the respective product `values.yaml` are configured with default `cpu` and `memory` [resource request values](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/).

### Resource requests
The default resource request that are used for each product are defined below. Using the [formula](#Memory-request-sizing), the `memory` specific values are derived from the default `JVM` requirements defined for each product's Docker container.

| Product  | CPU   |  Memory |
|----------|:-----:|------:|
| [Jira](https://bitbucket.org/atlassian-docker/docker-atlassian-jira/src/master/#markdown-header-memory-heap-size)                    | `1`   | `2G`  |
| [Confluence](https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/src/master/#markdown-header-memory-heap-size)   | `1`   | `2G`  |
| [Bitbucket](https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-server/src/master/)                                    | `1`   | `2G`  |
| [Crowd](https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/src/master/)                                                   | `1`   | `1G`  |

#### Memory request sizing
Request sizing must allow for the size of the product `JVM`. That means the `maximum heap size`, `minumum heap size` and the `reserved code cache size` (if applicable) plus other JVM overheads, must be considered when defining the request `memory` size. As a rule of thumb the formula below can be used to deduce the appropriate request memory size.
```shell
(maxHeap + codeCache) * 1.5
```

### Resource limits
Environmental and hardware constraints are different for each deployment, therefore the product `values.yaml` do not provide a resource [`limit`](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#resource-requests-and-limits-of-pod-and-container) definition. Resource usage limits can be defined by updating the commented out `resources.container.limits` stanza within the appropriate product `values.yaml`.

### Scaling resource requests and limits
The resource `requests` and `limits` can be defined either post product deployment or for deployments that are already running within the Kubernetes cluster

#### Prior to deployment
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

#### Post deployment
For existing deployments the `requests` and `limits` values can be dynamically updated. Using `kubectl edit` on the appropriate `statfulset` the respective `cpu` and `memory` values can be modified. Saving the changes will then result in the existing product pod(s) being re-provisioned with the updated values. 
