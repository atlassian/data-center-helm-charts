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

    To reduce the IO latency between the NFS server and Bitbucket Pod(s) it is  highly recommend to keep them in close proximity. To achieve this, you can use [standard Kubernetes affinity rules](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity){.external}. The `affinity` stanza within `values.yaml` can be updated to take advantage of this behaviour i.e.
    
    ```yaml
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

#### Uninstall
```shell
helm uninstall nfs-server --namespace nfs
```

## Update `values.yaml`
Get the IP address of the NFS service (`CLUSTER-IP`) by running the following command
```shell
kubectl get service --namespace nfs | awk '{print $3}'
```
!!!info "NFS directory share"

    The NFS Helm chart creates and exposes the directory share `/srv/nfs`. This will be required when configuring `values.yaml` 
The approach below shows how a `persistentVolume` and corresponding `peristentVolumeClaim` can be dynamically created for the provisioned NFS. Using the NFS IP and directory share, (see above) update the `values.yaml` appropriately:
```yaml
volumes:
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
      claimName: "custom-nfs-server-claim"
```
