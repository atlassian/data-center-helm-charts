# Available examples 

!!!warning "Support disclaimer"
    Use the examples we provide as reference only, we don’t offer official support for them. 

## Pre-requisites

### :material-kubernetes: Kubernetes clusters 
See examples of provisioning Kubernetes clusters on cloud-based providers:
   
  * [Amazon EKS](cluster/EKS_SETUP.md) 
  * [Google GKE](cluster/GKE_SETUP.md)
  * [Azure AKS](cluster/AKS_SETUP.md)

### :material-directions-fork: Ingress
* See an example of [provisioning an NGINX Ingress controller](ingress/INGRESS_NGINX.md)

### :material-database: Database
* See an example of [creating an Amazon RDS database instance](database/AMAZON_RDS.md)

### :material-folder-network: Storage
=== "AWS EBS"

    * See an example of [local storage utilizing AWS EBS-backed volumes](storage/aws/LOCAL_STORAGE.md)

=== "AWS EFS"

    * See an example of [shared storage utilizing AWS EFS-backed filesystem](storage/aws/SHARED_STORAGE.md)

=== "NFS"

    * See an example of [standing up an NFS server for Bitbucket](storage/nfs/NFS.md)

## Bamboo

### :material-lan-pending: Remote agents
* See an example of deploying a [remote agent for Bamboo](bamboo/REMOTE_AGENTS.md)

## Bitbucket

### :material-file-search-outline: Elasticsearch
* See an example of [standing up an Elasticsearch instance for Bitbucket](bitbucket/BITBUCKET_ELASTICSEARCH.md)

### :material-mirror-variant: Smart Mirrors
* See an example of [Bitbucket Smart Mirrors](bitbucket/BITBUCKET_MIRRORS.md)

### :material-remote-desktop: SSH
* See an example of [SSH service in Bitbucket on Kubernetes](bitbucket/BITBUCKET_SSH.md)

## Other

### :material-file-document-edit: Logging
* See an example of [how to deploy an EFK stack to Kubernetes](logging/efk/EFK.md)

### :material-checkbox-multiple-marked-outline: Customization
* See an example of [External libraries and plugins](external_libraries/EXTERNAL_LIBS.md)