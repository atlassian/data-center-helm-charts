# Shared storage
Atlassian's Data Center products require a shared storage solution to effectively operate in multi-node environment. The specifics of how this shared storage is created is site-dependent, we do however provide examples on how shared storage can be created below.

!!!tip "In case you are not using [Bitbucket Mesh](../bitbucket/BITBUCKET_MESH.md) nodes, Bitbucket requires a **dedicated NFS server** providing persistence for a shared home due to requirements for high IO throughput. See [NFS](nfs/NFS.md) example for details"

## AWS EFS
Jira, Confluence and Crowd can all be configured with an EFS-backed shared solution. For details on how this can be set up, see the [AWS EFS example](aws/SHARED_STORAGE.md). 
## NFS
For details on creating shared storage for Bitbucket, see the [NFS example](nfs/NFS.md).
