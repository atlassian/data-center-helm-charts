# Reference infrastructure

For full production deployment you will need to provide some common components that
are then passed as values to the helm chart when installing. These components are:

* Shared storage
* Database
* Elasticsearch (optional)

## Shared storage

### Cloud managed shared storage

#### Requirements

#### Example


### NFS server - Bitbucket DC requirement

Bitbucket Data Center uses a shared network file system (NFS) to store its repositories in a common location that is accessible to multiple Bitbucket nodes.

#### Requirements

Prior to installing the Helm chart, a suitable NFS shared storage solution must be provisioned. The exact details of this resource will be highly site-specific, but the example below can be used as a guide.

For more information on setting up Bitbucket Data Center's shared file server, see [Step 2. Provision your shared file system](https://confluence.atlassian.com/bitbucketserver/install-bitbucket-data-center-872139817.html#InstallBitbucketDataCenter-nfs). This section contains the requirements and recommendations for setting up NFS for Bitbucket Data Center.

#### Example

We've provided the template `./storage/nfs/nfs-server.yaml` as a **reference** on how an NFS server could be set-up to work in conjunction with a BitBucket deployment.  

:warning: Please note that the NFS server created with this template is not production ready and should not be used for anything other than example material.
