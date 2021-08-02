# Shared storage
Atlassian's Data Center products require a shared storage solution to effectively operate in multi-node environment. The specifics of how this shared storage is created is site-dependent, we do however provide examples on how shared storage can be created below.

> **NOTE:** Of all the Atlassian products, Bitbucket's shared storage solution must be NFS based. See [Bitbucket NFS below](#bitbucket-NFS) for details. 

## AWS EFS
Jira, Confluence and Crowd can all be configured with an EFS-backed shared solution. For details on how this can be set up, see the [AWS EFS example](aws/SHARED_STORAGE.md). 
## NFS  
For details on creating shared storage for Bitbucket, see the [NFS example](nfs/NFS.md).
