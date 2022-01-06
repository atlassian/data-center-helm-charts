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
There are a number of known limitations relating to Bamboo Data Center, these are documented below.

### Deployment
With [Bamboo DC 8.1](https://confluence.atlassian.com/bamboo/bamboo-8-1-release-notes-1103070461.html){.external} deployments to K8s using the Helm charts are now possible. This release does however contain an issue where [partial unattended deployments to K8s do not work](https://jira.atlassian.com/browse/BAM-21542){.external}. 

!!!info "Unattended setup"
  
    Until [this issue](https://jira.atlassian.com/browse/BAM-21542){.external} has been resolved, the recommended approach for deploying Bamboo server is using an `unattended` approach. That is, providing values to all those properties labeled as `REQUIRED` and `UNATTENDED-SETUP` within the `values.yaml`. This has the added benefit of eliminating any manual intervention (via the setup wizard) required for configuring Bamboo post deployment.

    It should also be noted that the property, `bamboo.unattendedSetup` should be set to `true` (current default value) for this to work.

### Cluster size
At present Bamboo Data Center utilizes an [active-passive clustering model](https://confluence.atlassian.com/bamboo/clustering-with-bamboo-data-center-1063170551.html){.external}. This architecture is not ideal where K8s deployments are concerned.

!!!warning "1 pod clusters only" 
  
    At present, Bamboo server cluster sizes comprising only `1` pod is the only supported topology for now.

### Server and agent affinity
The Bamboo server and Bamboo agents must be deployed to the same cluster. You cannot have Bamboo agents in one cluster communicating with a Bamboo server in another.

### Bamboo to Cloud App Link
When configuring application links between Bamboo server and any Atlassian Cloud server product, the Bamboo server base URL needs to be used. [See public issue for more detail](https://jira.atlassian.com/browse/BAM-21439).

### Import and export of large datasets
At present there is an issue with Bamboo where the `/server` and `/status` endpoints become un-usable when performing an [export or import of large datasets](https://jira.atlassian.com/browse/BAM-18673){.external}. 

!!!info "Data migration"

    For large Bamboo instances we recommend using native database and filesystem backup tools instead of the built in [export](https://confluence.atlassian.com/bamboo/exporting-data-for-backup-289277255.html){.external} / [import](https://confluence.atlassian.com/bamboo/importing-data-from-backup-289277260.html){.external} functionality that Bamboo provides. See the [migration guide](../userguide/MIGRATION.md) for more details.

The Bamboo Helm chart does however provide a facility that can be used to import [data exports produced through Bamboo](https://confluence.atlassian.com/bamboo/exporting-data-for-backup-289277255.html){.external} at deployment time. This can be used by configuring the Bamboo server `values.yaml` appropriately i.e. 

```yaml
import:
  type: import
  path: "/var/atlassian/application-data/shared-home/bamboo-export.zip"
```

Using this approach will restore the full dataset as part of the Helm install process.

## Platform limitations
These configurations are explicitly not supported, and the Helm charts don’t work without modifications in these environments:


* [Istio infrastructure](https://istio.io/latest/docs/ops/deployment/architecture/){.external}
    * Due to several reasons, Istio is imposing networking rules on every workload in the Kubernetes cluster that doesn't work with our deployments.
    * The current recommendation is to create an exemption for our workloads if Istio is enabled in the cluster by default.
* Air-tight network (no outgoing requests)
    * Some of our components are installed from publicly available repositories. When they can't reach the internet, they won’t work.

