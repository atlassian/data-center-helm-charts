# Resource requests and limits
To ensure that Kubernetes appropriately schedules resources, the respective product `values.yaml` is configured with default `cpu` and `memory` [resource request values](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/){.external} .

## Resource requests
The default resource requests that are used for each product are defined below. 
>Note: these values are geared towards small data sets. For larger enterprise deployments refer to the [Data Center infrastructure recommendations](https://confluence.atlassian.com/enterprise/data-center-infrastructure-recommendations-972333478.html){.external}.

Using the [formula](#memory-request-sizing) below, the `memory` specific values are derived from the default `JVM` requirements defined for each product's Docker container.

| Product  | CPU   |  Memory |
|----------|:-----:|------:|
| [Jira](https://bitbucket.org/atlassian-docker/docker-atlassian-jira/src/master/#markdown-header-memory-heap-size){.external}                        | `2`   | `2G`  |
| [Confluence](https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/src/master/#markdown-header-memory-heap-size){.external}     | `2`   | `2G`  |
| [Bitbucket](https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-server/src/master/){.external}                                        | `2`   | `2G`  |
| [Bamboo](https://bitbucket.org/atlassian-docker/docker-bamboo-server/src/master/){.external}                                                      | `2`   | `2G`  |
| [Bamboo agent](https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base/src/master/){.external}                                                      | `1`   | `2G`  |
| [Crowd](https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/src/master/){.external}                                                       | `2`   | `1G`  |

### Synchrony container resources

A Confluence cluster requires a Synchrony container. The default resource requests that are used for the Synchrony container are:

* `cpu: 2`
* `memory: 2.5G` 

### Memory request sizing
Request sizing must allow for the size of the product `JVM`. That means the `maximum heap size`, `minumum heap size` and the `reserved code cache size` (if applicable) plus other JVM overheads, must be considered when defining the request `memory` size. As a rule of thumb the formula below can be used to deduce the appropriate request memory size.
```shell
(maxHeap + codeCache) * 1.5
```

## Resource limits
Environmental and hardware constraints are different for each deployment, therefore the product's `values.yaml` does not provide a resource [`limit`](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#resource-requests-and-limits-of-pod-and-container){.external} definition. Resource usage limits can be defined by updating the commented out the `resources.container.limits` stanza within the appropriate `values.yaml`. For example:

```yaml
container:
  limits:
    cpu: "2"
    memory: "4G"
  requests:
    cpu: "2" 
    memory: "2G"
```
