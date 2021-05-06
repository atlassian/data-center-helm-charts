## Upgrade Application

### Kubernetes update strategies:
Kubernetes provides two strategies to update applications manage by `statefulset` controllers:

####RollingUpdate:
The pods will be upgraded one by one until all pods run containers with the updated template. The upgrade is managed by 
Kubernetes and the user has limited control in the upgrade process after modifying the template. This is the default 
upgrade strategy in Kubernetes. 
To perform a canary or multi-phase upgrade, a partition can be defined on the cluster and Kubernetes will upgrade just 
the nodes in that partition. 

####OnDelete: 
In this strategy users select the pod to upgrade by deleting it and Kubernetes will replace it by creating a new pod
 based on the updated template. To select this strategy the following changes should be applied in `statefulset` spec:

```yaml
  updateStrategy:
    type: OnDelete
```
Our implementation is based on *RollingUpdate* strategy with no *partition* defined.   

##Bitbucket
To learn about RollingUpgrade in Bitbucket see [Bitbucket RollingUpgrade](bitbucket-upgrade.md)

##Confluence
To learn about RollingUpgrade in Confluence see [Confluence RollingUpgrade](confluence-upgrade.md)