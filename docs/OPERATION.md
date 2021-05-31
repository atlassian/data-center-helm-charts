# Operation
Once you have [installed your product](INSTALLATION.md), use this document if you want to scale your product, update your product, or see what examples we have.

## Product scaling
For optimum performance and stability the appropriate resource `requests` and `limits` should be defined for each pod. The number of pods in the product cluster should also be carefully considered. Kubernetes provides means for horizontal and vertical scaling of the deployed pods within a cluster, these approaches are described below.

### Horizontal scaling - adding pods
The Helm charts provision one `StatefulSet` by default. The number of replicas within this StatefulSet can be altered either declaratively or intrinsically. Note that the Ingress must support cookie-based session affinity in order for the products to work correctly in a multi-node configuration.

#### Declaratively
1. Update `values.yaml` by modifying the `replicaCount` appropriately.
2. Apply the patch:
```shell
helm upgrade <release> <chart> -f <values file>
```

#### Intrinsically
```shell
kubectl scale statefulsets <statefulsetset-name> --replicas=n
```
Note: Confluence, Jira, and Cloud all require manual configuration after the first pod is deployed and before scaling up to additional pods, therefore when you deploy the product only one pod (replica) is created. The initial number of pods that should be started at deployment of each product is set in the replicaCount found in the values.yaml and should always be kept as 1.

For details on modifying the `cpu` and `memory` requirements of the `StatfuleSet` see section [Vertical Scaling](#Vertical-scaling) below. Additional details on the resource requests and limits used by the `StatfulSet` can be found in [REQUESTS_AND_LIMITS.md](resource_management/REQUESTS_AND_LIMITS.md).

### Vertical scaling - adding resources
The resource `requests` and `limits` for a `StatefulSet` can be defined before product deployment or for deployments that are already running within the Kubernetes cluster. Take note that vertical scaling will result in the pod being re-created with the updated values.

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
For existing deployments the `requests` and `limits` values can be dynamically updated either declaratively or intrinsically 

#### Declaratively
This the preferred approach as it keeps the state of the cluster, and the helm charts themselves in sync.
1. Update `values.yaml` appropriately
2. Apply the patch:
```shell
helm upgrade <release> <chart> -f <values file>
```

#### Intrinsically
Using `kubectl edit` on the appropriate `StatefulSet` the respective `cpu` and `memory` values can be modified. Saving the changes will then result in the existing product pod(s) being re-provisioned with the updated values.

## Product update
### Kubernetes update strategies
Kubernetes provides two strategies to update applications managed by `statefulset` controllers:

#### Rolling Update:
The pods will be upgraded one by one until all pods run containers with the updated template. The upgrade is managed by 
Kubernetes and the user has limited control during the upgrade process, after having modified the template. This is the default 
upgrade strategy in Kubernetes. 

To perform a canary or multi-phase upgrade, a partition can be defined on the cluster and Kubernetes will upgrade just 
the nodes in that partition. 

The default implementation is based on *RollingUpdate* strategy with no *partition* defined. 

#### OnDelete: 
In this strategy users select the pod to upgrade by deleting it, and Kubernetes will replace it by creating a new pod
 based on the updated template. To select this strategy the following should be replaced with the current 
 implementation of `updateStrategy` in the `statefulset` spec:

```yaml
  updateStrategy:
    type: OnDelete
```  

### Bitbucket Rolling Upgrade
To learn about rolling upgrade in Bitbucket see [Bitbucket RollingUpgrade](product_upgrades/BITBUCKET_UPGRADE.md)

### Confluence Rolling Upgrade
To learn about rolling upgrade in Confluence see [Confluence RollingUpgrade](product_upgrades/CONFLUENCE_UPGRADE.md)

## Examples
### Logging
#### How to deploy an EFK stack to Kubernetes
There are different methods to deploy an EFK stack. We provide two deployment methods, the first is deploying EFK locally on Kubernetes, and the second is using managed Elasticsearch outside the Kubernetes cluster. Please refer to [Logging in Kubernetes](examples/logging/efk/EFK.md).

### Storage
#### Example implementation of NFS Server for Bitbucket
Bitbucket Data Center (Bitbucket DC) uses a shared network file system (NFS) to store its repositories in a common location that is accessible to multiple Bitbucket nodes.

Prior to installing the Helm chart, a suitable NFS shared storage solution must be provisioned. The exact details of this resource will be highly site-specific, but we provide an example implementation of [NFS Server for Bitbucket](examples/storage/nfs/NFS.md), which can be used as a guide.

### Examples of provisioning storage with the AWS CSI Driver
 * [Local storage - utilizing AWS EBS-backed volumes](examples/storage/aws/LOCAL_STORAGE.md)
 * [Shared storage - utilizing AWS EFS-backed filesystem](examples/storage/aws/SHARED_STORAGE.md)

***
* Go back to the [installation guide](INSTALLATION.md)
* Dive deeper into the [configuration](CONFIGURATION.md) options
* Go back to [README.md](../README.md)
