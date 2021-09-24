# Operation
Once you have [installed your product](../userguide/INSTALLATION.md), use this document if you want to scale your product, update your product, or see what examples we have.

## Managing resources

You can scale your application by [adding additonal pods](resource_management/RESOURCE_SCALING.md) or by [managing available resources with requests and limits](resource_management/RESOURCE_SCALING.md).

## Upgrading application

### Kubernetes update strategies
Kubernetes provides two strategies to update applications managed by `statefulset` controllers:

#### Rolling update
The pods will be upgraded one by one until all pods run containers with the updated template. The upgrade is managed by 
Kubernetes and the user has limited control during the upgrade process, after having modified the template. This is the default 
upgrade strategy in Kubernetes. 

To perform a canary or multi-phase upgrade, a partition can be defined on the cluster and Kubernetes will upgrade just 
the nodes in that partition. 

The default implementation is based on *RollingUpdate* strategy with no *partition* defined. 

#### OnDelete strategy
In this strategy users select the pod to upgrade by deleting it, and Kubernetes will replace it by creating a new pod
 based on the updated template. To select this strategy the following should be replaced with the current 
 implementation of `updateStrategy` in the `statefulset` spec:

```yaml
  updateStrategy:
    type: OnDelete
```  

### Upgrade

* To learn about upgrading the Helm charts see [Helm chart upgrade](upgrades/HELM_CHART_UPGRADE.md).  
* To learn about upgrading the products without upgrading the Helm charts see [Products upgrade](upgrades/PRODUCTS_UPGRADE.md).


## Examples
### Logging
#### How to deploy an EFK stack to Kubernetes
There are different methods to deploy an EFK stack. We provide two deployment methods, the first is deploying EFK locally on Kubernetes, and the second is using managed Elasticsearch outside the Kubernetes cluster. Please refer to [Logging in Kubernetes](../examples/logging/efk/EFK.md).
