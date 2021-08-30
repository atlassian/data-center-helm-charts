# Limitations 

## Product limitations
We haven't changed our Data Center applications' architecture to support Kubernetes. So, as is with all our Data Center products, the following limitiations still exist:

* We don't support horizontal or vertical autoscaling in our products. Read about [Product scaling](../userguide/resource_management/RESOURCE_SCALING.md).
* More pods doesn’t mean that the application will be more performant.
* We still have session affinity, so you will need to have a network setup that supports that. 

## Platform limitations
These configurations are explicitly not supported and the Helm charts don’t work without modifications in these environments:

* [Istio infrastructure](https://istio.io/latest/docs/ops/deployment/architecture/){.external}
    * Due to several reasons, Istio is imposing networking rules on every workload in the Kubernetes cluster that doesn’t work with our deployments.
    * The current recommendation is to create an exemption for our workloads if Istio is enabled in the cluster by default.
* Air-tight network (no outgoing requests)
    * Some of our components are installed from publicly available repositories. When they can't reach the internet, they won’t work.

