# Resource requests and limits
To ensure that Kubernetes appropriately schedules resources, the respective product `values.yaml` are configured with default `cpu` and `memory` [resource request values](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/).

:warning: The default values defined in the product `values.yaml` are representative of small product workloads. For larger workloads please see<?LINK IN NASSER>

## Resource requests
The default resource request that are used for each product are defined below. Using the [formula](#Memory-request-sizing), the `memory` specific values are derived from the default `JVM` requirements defined for each product's Docker container.

| Product  | CPU   |  Memory |
|----------|:-----:|------:|
| [Jira](https://bitbucket.org/atlassian-docker/docker-atlassian-jira/src/master/#markdown-header-memory-heap-size)                    | `1`   | `2G`  |
| [Confluence](https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/src/master/#markdown-header-memory-heap-size)   | `1`   | `2G`  |
| [Bitbucket](https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-server/src/master/)                                    | `1`   | `2G`  |
| [Crowd](https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/src/master/)                                                   | `1`   | `1G`  |

### Memory request sizing
Request sizing must allow for the size of the product `JVM`. That means the `maximum heap size`, `minumum heap size` and the `reserved code cache size` (if applicable) plus other JVM overheads, must be considered when defining the request `memory` size. As a rule of thumb the formula below can be used to deduce the appropriate request memory size.
```shell
(maxHeap + codeCache) * 1.5
```

## Resource limits
Environmental and hardware constraints are different for each deployment, therefore the product `values.yaml` do not provide a resource [`limit`](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#resource-requests-and-limits-of-pod-and-container) definition. Resource usage limits can be defined by updating the commented out `resources.container.limits` stanza within the appropriate product `values.yaml`.

## Scaling resource requests and limits
The resource `requests` and `limits` can be defined either post product deployment or for deployments that are already running within the Kubernetes cluster

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
For existing deployments the `requests` and `limits` values can be dynamically updated. Using `kubectl edit` on the appropriate `statfulset` the respective `cpu` and `memory` values can be modified. Saving the changes will then result in the existing product pod(s) being re-provisioned with the updated values. 