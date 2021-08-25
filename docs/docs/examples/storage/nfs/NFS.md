# NFS server for Bitbucket
!!!danger "Disclaimer"

    **This functionality is not officially supported. It should not be used for production deployments!**
    
    The included NFS example is provided as is and should be used as reference a only. Before you proceed we highly recommend that you understand your specific deployment needs and tailor your solution to them.

## Bitbucket Data Center and NFS

Bitbucket Data Center uses shared home to store its repositories in a common location that is accessible to multiple Bitbucket nodes. 
Due to the high requirements on performance for IO operations, Bitbucket needs a dedicated NFS server providing persistence for a shared home. Based on this, 
we don't recommend that you use 
[cloud managed storage services](https://confluence.atlassian.com/bitbucketserver/supported-platforms-776640981.html#Supportedplatforms-cloudplatformsCloudPlatforms) such as AWS EFS.
 
## NFS provisioning
The NFS server can be provisioned manually or by using the supplied Helm chart. Details for both approaches can be found below.

!!!tip "Pod affinity"

    It is  **highly recommend** keep the NFS server and Bitbucket nodes in close proximity. To achieve this, you can use [standard Kubernetes affinity rules](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity){.external}. The `affinity` stanza within `values.yaml` can be updated to take advantage of this behaviour.
    
    ```yaml
    # -- Standard Kubernetes affinities that will be applied to all Bitbucket pods
    # Due to the performance requirements it is highly recommended running all Bitbucket pods
    # in the same availability zone as your dedicated NFS server. To achieve this, you
    # can define `affinity` and `podAffinity` rules that will place all pods into the same zone,
    # and therefore minimise the physical distance between the application pods and the shared storage.
    # More specific documentation can be found in the official Affinity and Anti-affinity documentation:
    # https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity
    #
    # This is an example on how to ensure the pods are in the same zone as NFS server that is labeled with `role=nfs-server`:
    #
    #   podAffinity:
    #    requiredDuringSchedulingIgnoredDuringExecution:
    #      - labelSelector:
    #          matchExpressions:
    #            - key: role
    #              operator: In
    #              values:
    #                - nfs-server # needs to be the same value as NFS server deployment
    #        topologyKey: topology.kubernetes.io/zone
    affinity: {}
    ```

### Manual
For information on setting up Bitbucket Data Center's shared file server, see [Provision your shared file system](https://confluence.atlassian.com/bitbucketserver/install-bitbucket-data-center-872139817.html#InstallBitbucketDataCenter-nfs){.external}. 
This section contains the requirements and recommendations for setting up NFS for Bitbucket Data Center.

!!!tip "NFS Server sizing"

    Ensure the NFS server's size is appropriate for the needs of the Bitbucket instance. See [capacity recommendations](https://confluence.atlassian.com/bitbucketserver/recommendations-for-running-bitbucket-in-aws-776640282.html){.external} for details.

### Helm
!!!danger "Disclaimer"

    **This Helm chart is not officially supported! It should not be used for production deployments!**

#### Installation
Clone this repo and from the sub-directory, `data-center-helm-charts/docs/docs/examples/storage/nfs`, run the following command:
```shell
helm install nfs-server nfs-server-example --namespace nfs
```
This will create an K8s service, its IP address will be used for configuring the `values.yaml`. The IP corresponds to the `CLUSTER-IP` returned when running:
```shell
kubectl get service --namespace nfs
```
!!!info "NFS directory share"

    The NFS Helm chart creates and exposes the directory share `/srv/nfs`. This will be required when configuring `values.yaml` 

#### Uninstall
```shell
helm uninstall nfs-server --namespace nfs
```

## Update `values.yaml`
Regardless of the approach used for installing the NFS the `values.yaml` needs to be updated as follows.
```yaml
sharedHome:
  persistentVolume:
    create: true
    nfs:
      server: "10.100.197.23" # IP address of the NFS server 
      path: "/srv/nfs" # Directory share of NFS
  persistentVolumeClaim:
    create: true
    storageClassName: ""
```
You can of course manually provision your own `persistentVolume` and corresponding claim (as opposed to the dynamic approach described above) for the NFS server. In this case update the `values.yaml` to make use of them via the `customVolume` stanza:
```yaml
sharedHome:
  customVolume: 
    persistentVolumeClaim:
      claimName: "nfs-server-claim"
```
