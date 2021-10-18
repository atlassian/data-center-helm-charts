# Prerequisites 
## Requirements 

In order to deploy Atlassian’s Data Center products, the following is required:

1. An understanding of [Kubernetes](https://kubernetes.io/docs/concepts/overview/what-is-kubernetes/){.external} and [Helm](https://helm.sh/){.external} concepts.
2. [`kubectl` `v1.19` or later](https://kubernetes.io/docs/tasks/tools/), must be compatible with your cluster.
3. [`helm` `v3.3` or later.](https://helm.sh/docs/intro/install/)

## Environment setup 

Before installing the Data Center Helm charts you need to set up your environment:

1. [Create and connect to the Kubernetes cluster](#create-and-connect-to-the-kubernetes-cluster)
2. [Provision an Ingress Controller](#provision-an-ingress-controller)
3. [Provision a database](#provision-a-database)
4. [Configure a shared-home volume](#configure-a-shared-home-volume)
5. [Configure a local-home volume](#configure-local-home-volume)

!!!info "Elasticsearch for Bitbucket"
    We highly recommend you use an external Elasticsearch installation for Bitbucket. When you run more than one node you need to have a separate Elasticsearch cluster to enable code search. See [Bitbucket Elasticsearch recommendations](../examples/bitbucket/BITBUCKET_ELASTICSEARCH.md). 
    
---

### :material-kubernetes: Create and connect to the Kubernetes cluster

* In order to install the charts to your Kubernetes cluster (version 1.19+), your Kubernetes client config must be configured appropriately, and you must have the necessary permissions.
* It is up to you to set up security policies.

!!!example ""
      See examples of [provisioning Kubernetes clusters on cloud-based providers](../examples/cluster/CLOUD_PROVIDERS.md).

### :material-directions-fork: Provision an Ingress Controller

* This step is necessary in order to make your Atlassian product available from outside of the Kubernetes cluster after deployment. 
* The Kubernetes project supports and maintains ingress controllers for the major cloud providers including; [AWS](https://github.com/kubernetes-sigs/aws-load-balancer-controller#readme){.external}, [GCE](https://github.com/kubernetes/ingress-gce/blob/master/README.md#readme){.external} and [nginx](https://github.com/kubernetes/ingress-nginx/blob/master/README.md#readme){.external}. There are also a number of open-source [third-party projects available](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/){.external}.
* Because different Kubernetes clusters use different ingress configurations/controllers, the Helm charts provide [Ingress Object](https://kubernetes.io/docs/concepts/services-networking/ingress/){.external} templates only.
* The Ingress resource provided as part of the Helm charts is geared toward the [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/){.external} and can be configured via the `ingress` stanza in the appropriate `values.yaml` (an alternative controller can be used).
* For more information about the Ingress controller go to the [Ingress section of the configuration guide](CONFIGURATION.md#ingress).

!!!example ""
      See an example of [provisioning an NGINX Ingress Controller](../examples/ingress/CONTROLLERS.md).

### :material-database: Provision a database

* Must be of a type and version supported by the Data Center product you wish to install:
  
=== "Jira"
      [Supported databases](https://confluence.atlassian.com/adminjiraserver/supported-platforms-938846830.html#Supportedplatforms-Databases){.external}
=== "Confluence"
      [Supported databases](https://confluence.atlassian.com/doc/supported-platforms-207488198.html#SupportedPlatforms-Databases){.external}
=== "Bitbucket"
      [Supported databases](https://confluence.atlassian.com/bitbucketserver/supported-platforms-776640981.html#Supportedplatforms-databasesDatabases){.external}
=== "Bamboo"
      [Supported databases](https://confluence.atlassian.com/bamboo/supported-platforms-289276764.html#Supportedplatforms-Databases){.external}
=== "Crowd"
      [Supported databases](https://confluence.atlassian.com/crowd/supported-platforms-191851.html#SupportedPlatforms-Databases){.external}

* Must be reachable from the product deployed within your Kubernetes cluster. 
* The database service may be deployed within the same Kubernetes cluster as the Data Center product or elsewhere.
* The products need to be provided with the information they need to connect to the database service. Configuration for each product is mostly the same, with some small differences. For more information go to the [Database connectivity section of the configuration guide](CONFIGURATION.md#database-connectivity).

!!!info "Reducing pod to database latency" 

      For better performance consider co-locating your database in the same Availability Zone (AZ) as your product nodes. Database-heavy operations, such as full re-index, become significantly faster when the database is collocated with the Data Center node in the same AZ. However we don't recommend this if you're running critical workloads.

!!!example ""
      See an example of [provisioning databases on cloud-based providers](../examples/database/CLOUD_PROVIDERS.md).


### :material-folder-network: Configure a shared-home volume
* All of the Data Center products require a shared network filesystem if they are to be operated in multi-node clusters. If no shared filesystem is available, the products can only be operated in single-node configuration.
* Some cloud based options for a shared filesystem include [AWS EFS](https://aws.amazon.com/efs/){.external} and [Azure Files](https://docs.microsoft.com/en-us/azure/storage/files/storage-files-introduction){.external}. You can also stand up your own NFS
* The logical representation of the chosen storage type within Kubernetes can be defined as `PersistentVolumes` with an associated `PersistentVolumeClaims` in a `ReadWriteMany (RWX)` access mode.
* For more information about volumes see the [Volumes section of the configuration guide](CONFIGURATION.md#volumes). 

!!!example ""
      See examples of [creating shared storage](../examples/storage/STORAGE.md).

### :material-folder-home: Configure local-home volume
* As with the [shared-home](#configure-a-shared-home-volume), each pod requires its own volume for `local-home`. Each product needs this for defining operational data. 
* If not defined, an [emptyDir](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir){.external} will be utilised. 
* Although an `emptyDir` may be acceptable for evaluation purposes, we recommend that each pod is allocated its own volume.
* A `local-home` volume could be logically represented within the cluster using a `StorageClass`. This will dynamically provision an [AWS EBS](https://aws.amazon.com/ebs/?ebs-whats-new.sort-by=item.additionalFields.postDateTime&ebs-whats-new.sort-order=desc){.external} volume to each pod.

!!!example ""
      An example of this strategy can be found [the local storage example](../examples/storage/aws/LOCAL_STORAGE.md).
