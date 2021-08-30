# Support boundaries

This page describes what is within our scope of support for Kubernetes deployments, and what isn't. 

!!!note "Additional information"
    Read our [troubleshooting tips](TROUBLESHOOTING.md).

    Read about the [product and platform limitations](LIMITATIONS.md).


## Supported components
### Helm charts

Helm is a Kubernetes package manager. It allows us to provide generic YAML templates that you configure with the specific values for your environments.

You are responsible for creating the components that are required by the product for your type of deployment, as described in the [Prerequisites](../userguide/PREREQUISITES.md). You need to insert the values (hostnames, configuration values) into your specific `values.yaml` file that is used for installation. We provide documentation for different configuration options in the [Configuration guide](../userguide/CONFIGURATION.md) and the [Examples](../examples/EXAMPLES.md).

If you have followed our documentation on how to configure the Helm charts, and you're using correctly created components, we will provide support if you encounter an error in installation. 

If you find any issues, [raise a ticket](https://github.com/atlassian/data-center-helm-charts/issues/new){.external}. If you have general feedback or questions regarding the charts, use [Atlassian Community Kubernetes space](https://community.atlassian.com/t5/Atlassian-Data-Center-on/gh-p/DC_Kubernetes){.external}.

## Unsupported components
There is a set of required components that you can create in multiple ways. You are responsible for creating them correctly so you can use them in the Helm charts. 

### Kubernetes cluster
You need to make sure that you have enough privileges to run the application and create all the necessary entities that the Helm charts require. There are also different Kubernetes flavours that might require specific knowledge of how to install the products in them. For example, OpenShift and Rancher have more strict rules regarding container permissions.

See examples of [provisioning Kubernetes clusters on cloud-based providers](../examples/cluster/CLOUD_PROVIDERS.md).

### Shared storage
Kubernetes setup requires you to have shared storage if you want to have a clustered instance. It's completely up to you how you set up the shared storage. The main requirement is that this storage needs to be accessible from Kubernetes and needs to be accessible from multiple pods. 

You can use a managed storage solution like EFS, Azure files, or some other dedicated solution that provides NFS-like access (e.g. dedicated NFS server, NetApp).

There is a large number of combinations and potential setup scenarios and we can't support all of them. Our Helm charts expect you to provide a persistent volume claim or a similar accessible shared storage in the `values.yaml` file.

See examples of [creating shared storage](../examples/storage/STORAGE.md). For more information about volumes go to the [Volumes section of the configuration guide](../userguide/CONFIGURATION.md#volumes). 


### Networking
You're required to configure the network access to the cluster. In Kubernetes, this usually means providing an ingress controller. See an example of [provisioning an NGINX Ingress controller](../examples/ingress/CONTROLLERS.md). 

It is up to you to make sure that the network configuration doesn’t prevent nodes from communicating with each other and other components.

You also need to make sure that your instance is accessible to the users (DNS, firewalls, VPC config).

### Database
The database is provided as a connection string with the option to provide credentials and a driver. The database needs to be configured following product-specific requirements as per usual and needs to be accessible from Kubernetes.

See an example of [provisioning databases on cloud-based providers](../examples/database/CLOUD_PROVIDERS.md).
