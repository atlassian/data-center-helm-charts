# Limitations 

## Product limitations
We haven't changed our Data Center applications' architecture to support Kubernetes. So, as is with all our Data Center products, the following limitations still exist:

* We don't support horizontal or vertical autoscaling in our products. Read about [Product scaling](../userguide/resource_management/RESOURCE_SCALING.md).
* More pods doesn't mean that the application will be more performant.
* We still have session affinity, so you will need to have a network setup that supports that. 

## Jira and horizontal scaling
At present there are issues relating to index replication with Jira when immediately scaling up by more than 1 pod at a time.

* [Index replication service is paused indefinitely](https://jira.atlassian.com/browse/JRASERVER-72125){.external}
* [Automatic restore of indexes will fail ](https://jira.atlassian.com/browse/JRASERVER-62669){.external}

!!!info "Indexing improvements" 
  
    Please note that Jira is actively being worked on to address these issues in the coming releases.
      
Although these issues are Jira specific, they are exasperated on account of the significantly reduced startup times for Jira when running in a Kubernetes cluster. As such these issues can have an impact on horizontal scaling if [you don't take the correct approach](../../userguide/resource_management/RESOURCE_SCALING/#scaling-jira-safely).

## Bamboo

!!!warning "Under active development"
    
    Bamboo is currently under active development and should not be used for production based deployments.


There are a number of known limitations relating to Bamboo Data Center, these are documented below.

### Deployment
Support for Bamboo on K8s is now officially provided with the release of [Bamboo DC 8.1](https://confluence.atlassian.com/bamboo/bamboo-8-1-release-notes-1077903836.html){.external}. This release contains an issue where [partial unattended installations of Bamboo DC to K8s clusters do not work](https://jira.atlassian.com/browse/BAM-21542){.external}. 

!!!info "Unattended setup"
  
    Until this issue has been resolved, the recommended approach for deploying Bamboo server is using an `unattended` approach. That is, providing values to all those properties labeled as `REQUIRED` and `UNATTENDED` within the `values.yaml`. This has the added benefit of eliminating any manual intervention (via the setup wizard) required for configuring Bamboo post deployment.

    It should also be noted that the property, `bamboo.unattendedSetup` should be set to `true` (current default value) for this to work.

### Cluster size
At present Bamboo Data Center utilizes an [active-passive clustering model](https://confluence.atlassian.com/bamboo/clustering-with-bamboo-data-center-1063170551.html){.external}. This architecture is not ideal where K8s deployments are concerned. As such a Bamboo server cluster comprising only `1` pod is the recommended topology for now.

### Server and agent affinity
The Bamboo server and Bamboo agents must be deployed to the same cluster. You cannot have Bamboo agents in one cluster communicating with a Bamboo server in another.

### Bamboo to Cloud App Link
When configuring application links between Bamboo server and any Atlassian Cloud server product, the Bamboo server base URL needs to be used. [See public issue for more detail](https://jira.atlassian.com/browse/BAM-21439).

### Import and export of large datasets
At present there is an issue with Bamboo where the `/server` and `/status` REST endpoints become un-usable when performing an [export or import of large datasets](https://jira.atlassian.com/browse/BAM-18673){.external}. 

!!!info "DB migration for dataset restore"

    If a data export/import is required this should be done [via a DB dump/restore](https://confluence.atlassian.com/bamboo/moving-your-bamboo-data-to-a      different-database-289277250.html#MovingyourBamboodatatoadifferentdatabase-AlternativeDBmigration){.external}

## Platform limitations
These configurations are explicitly not supported, and the Helm charts don’t work without modifications in these environments:


* [Istio infrastructure](https://istio.io/latest/docs/ops/deployment/architecture/){.external}
    * Due to several reasons, Istio is imposing networking rules on every workload in the Kubernetes cluster that doesn't work with our deployments.
    * The current recommendation is to create an exemption for our workloads if Istio is enabled in the cluster by default.
* Air-tight network (no outgoing requests)
    * Some of our components are installed from publicly available repositories. When they can't reach the internet, they won’t work.

