# Limitations 

## Product limitations
We haven't changed our Data Center applications' architecture to support Kubernetes. So, as is with all our Data Center products, the following limitiations still exist:

* We don't support horizontal or vertical autoscaling in our products. Read about [Product scaling](../userguide/resource_management/RESOURCE_SCALING.md).
* More pods doesn’t mean that the application will be more performant.
* We still have session affinity, so you will need to have a network setup that supports that. 

## Jira and horizontal scaling
At present there are issues relating to index replication with Jira when immediately scaling up by more than 1 pod at a time.

* [Index replication service is paused indefinitely](https://jira.atlassian.com/browse/JRASERVER-72125){.external}
* [Automatic restore of indexes will fail ](https://jira.atlassian.com/browse/JRASERVER-62669){.external}

!!!info "Indexing improvements" 
  
    Please note that Jira is actively being worked to address these issues in the coming releases.
      
Although these issues are Jira specific, they are exasperated on account of the significantly reduced startup times for Jira when running in a Kubernetes cluster. As such these issues can have an impact on horizontal scaling if [you don't take the correct approach](../../userguide/resource_management/RESOURCE_SCALING/#scaling-jira-safely).

## Platform limitations
These configurations are explicitly not supported and the Helm charts don’t work without modifications in these environments:

* [Istio infrastructure](https://istio.io/latest/docs/ops/deployment/architecture/){.external}
    * Due to several reasons, Istio is imposing networking rules on every workload in the Kubernetes cluster that doesn’t work with our deployments.
    * The current recommendation is to create an exemption for our workloads if Istio is enabled in the cluster by default.
* Air-tight network (no outgoing requests)
    * Some of our components are installed from publicly available repositories. When they can't reach the internet, they won’t work.

