# Implementation of an NFS Server for Bitbucket

!!!warning Dislaimer

    **This functionality is not officially supported.**
    
    The included examples are provided as is and are to be used as guidance on how to set up a testing environment. These exampes should not be used in production. 
    
    Before proceeding it is highly recommended that you understand your specific deployment needs and tailor your solution to them.

# Components

For a full production deployment you will need to create some common components that are then passed as values 
to the helm chart when installing. These components are:

* Shared storage
* Database
* Elasticsearch

## Shared storage

### Cloud-managed shared storage 

#### Dedicated NFS server - Bitbucket Data Center requirement

Bitbucket Data Center (Bitbucket DC) uses a shared network file system (NFS) to store its repositories in a common 
location that is accessible to multiple Bitbucket nodes. Due to the high requirements on performance for IO 
operations, Bitbucket needs a dedicated NFS server providing persistence for a shared home. Based on this, 
it is not recommended that 
[cloud managed storage services](https://confluence.atlassian.com/bitbucketserver/supported-platforms-776640981.html#Supportedplatforms-cloudplatformsCloudPlatforms) 
are used.

You might opt to use an NFS server for other Data Center products, but they don't have the same performance 
characteristics. It might be beneficial to prefer the resiliency of a managed service over a self-managed 
server for other products.
 
#### Requirements

Prior to installing the Helm chart, a suitable NFS shared storage solution must be provisioned. The exact details 
of this resource will be highly site-specific, but the example below can be used as a guide.

For more information on setting up Bitbucket Data Center's shared file server, see 
[Step 2. Provision your shared file system](https://confluence.atlassian.com/bitbucketserver/install-bitbucket-data-center-872139817.html#InstallBitbucketDataCenter-nfs){.external}. 
This section contains the requirements and recommendations for setting up NFS for Bitbucket Data Center.

Please read through the 
[capacity recommendations](https://confluence.atlassian.com/bitbucketserver/recommendations-for-running-bitbucket-in-aws-776640282.html)
to size your NFS server accordingly to your instance needs.

#### Example

We've provided the template [`nfs-server-example`](nfs-server-example) as a **reference** on how an NFS server could
be stood-up to work in conjunction with a Bitbucket deployment. 

Provision the NFS by issuing the following command:
```shell
helm install nfs-server-example nfs-server-example
```

:warning: Please note that the NFS server created with this template is not production ready and should not be 
used for anything other than testing deployment.


#### Pod affinity

It is **highly recommended** to keep NFS server and Bitbucket nodes in close proximity. To achieve this, you can use affinity rules -
a standard kubernetes functionality. You can read the [Kubernetes affinity documentation](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity){.external} and use the suitable affinity definition in the `affinity: {}` definition in the `values.yaml` file.
