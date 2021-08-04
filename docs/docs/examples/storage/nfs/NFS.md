# Implementation of an NFS Server for Bitbucket

!!!warning Dislaimer

    **This functionality is not officially supported.**
    
    The included examples are provided as is and are to be used as guidance on how to set up a testing environment. These exampes should not be used in production. 
    
    Before you proceed we highly recommend that you understand your specific deployment needs and tailor your solution to them.

# Components

For a full production deployment you will need to create some common components. These components are then passed as values 
to the Helm chart when you install your product. The components are:

* Shared storage
* Database
* Elasticsearch

## Shared storage

### Cloud-managed shared storage 

#### Dedicated NFS server - Bitbucket Data Center requirement

Bitbucket Data Center uses a shared network file system (NFS) to store its repositories in a common 
location that is accessible to multiple Bitbucket nodes. Due to the high requirements on performance for IO 
operations, Bitbucket needs a dedicated NFS server providing persistence for a shared home. Based on this, 
we don't recommend that you use 
[cloud managed storage services](https://confluence.atlassian.com/bitbucketserver/supported-platforms-776640981.html#Supportedplatforms-cloudplatformsCloudPlatforms).

You might choose to use an NFS server for other Data Center products, but they don't have the same performance 
characteristics. It might be better to go for the resilience of a managed service over a self-managed 
server for other products.
 
#### Requirements

Prior to installing the Helm chart, you need to provision a suitable NFS shared storage solution. The exact details 
of this resource will be highly site-specific, but the example below can be used as a guide.

For more information on setting up Bitbucket Data Center's shared file server, see 
[Step 2. Provision your shared file system](https://confluence.atlassian.com/bitbucketserver/install-bitbucket-data-center-872139817.html#InstallBitbucketDataCenter-nfs){.external}. 
This section contains the requirements and recommendations for setting up NFS for Bitbucket Data Center.

You need to set your NFS server's size according to your instance needs. See the capacity recommendations](https://confluence.atlassian.com/bitbucketserver/recommendations-for-running-bitbucket-in-aws-776640282.html).

#### Example

We've provided a template as a **reference** on how an NFS server could be stood-up to work in conjunction 
with a Bitbucket deployment: [`nfs-server-example`](nfs-server-example).

Provision the NFS by using the following command:
```shell
helm install nfs-server-example nfs-server-example
```

:warning: Please note that the NFS server created with this template is not production ready and should not be 
used for anything other than testing deployment.


#### Pod affinity

We **highly recommend** to keep NFS server and Bitbucket nodes in close proximity. To achieve this, you can use [standard Kubernetes affinity rules](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity){.external}. Use the suitable affinity definition in the `affinity: {}` definition in the `values.yaml` file.
