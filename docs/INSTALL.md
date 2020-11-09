# Prerequisites
* A Kubernetes cluster, running Kubernetes 1.17 or later
  * earlier versions may work, but haven't been tested
  * as of November 2020, 1.17 is the oldest maintained release of Kubernetes
* Helm 3.3 or later 
  * again, earlier versions may work, but have not been tested
* In order to install the charts to your Kubernetes cluster, your kubernetes client config must 
be configured appropriately, and you must have the necessary permissions.
* A database provisioned and ready to go
   * Must be of a type and version supported by the Data Center product you wish to install
   * Must be reachable from the product deployed within your Kubernetes cluster
   * The database service may be deployed within the same Kubernetes cluster as the Data Center product,
   or elsewhere.
* All of the Data Center products require a shared network filesystem if they are to 
be operated in multi-node clusters. 
   * If no shared filesystem is available, the products can only be operated in
   single-node configuration.

## Kubernetes pre-configuration
There are a few items that need to be pre-configured in your Kubernetes cluster, which the resources deployed 
by the Helm charts will expect to be present.
* Secrets
   * [all products] there must be a Secret in the target namespace containing
the username and password that the product should use to connect to the database. 
   * [confluence and bitbucket] there must be a Secret in the target namespace 
containing the license key for the product. Must be a valid Data Center license.
   * [bitbucket] there must be a Secret in the namespace containing the sysadmin user
credentials (username, password, display name and email address)
   * these may all be combined into a single Secret, or can be separate. 
   * The names of the secrets, and the value keys within those secrets, 
   can be anything you like, but there are defaults in the charts' `values.yaml` 
   files. These defaults can be used, otherwise the alternative names must be
   specified during installation.   
* Service account
   * [confluence and bitbucket] a Kubernetes service account must be configured 
   that should be used by the product. This account must have permission to query
   the Kubernetes API to discover other Data Center cluster nodes. 
* Volumes
   * [all products] Each Data Center node requires a "local home" Persistent Volume 
   with access mode `ReadWriteOnce`. These can be statically provisioned as required,
   or a dynamic provisioner can be used.  
   * [all products] All Data Center nodes in a given deployment must have access to
   a "shared home" PersistentVolume. If there is only a single Data Center cluster node,
   then `ReadWriteOnce` is sufficient access, but if there are to be multiple 
   Data Center nodes, then `ReadWriteMany` is required.
   * [all products] In addition to the shared-home PersistentVolume itself,
   a shared-home PersistentVolumeClaim must also be created.
* Ingress controller
   * Because different Kubernetes clusters use different Ingress configurations,
   the Helm charts will not install an Ingress resource. An ingress controller
   must be installed in the cluster, and an ingress resource using that controller
   must be provided post-installation.
   
# Installation
1. Add the Helm chart repository to your local Helm installation
   * `helm repo add atlassian-data-center https://atlassian-labs.github.io/data-center-helm-charts`
   * substitute `atlassian-data-center` with whatever name you wish
   * only needs to be run once
1. Install your chosen product
   * `helm install <release-name> atlassian-data-center/<product> --namespace <namespace> --version <chart-version> --values values.yaml`
      * `<release-name>` is up to you, or you can use `--generate-name`
      * `<product>` can be any one of `jira`, `confluence` or `bitbucket`
      * `<chart-version>` can be omitted if you just wish the latest version of the chart
      * `values.yaml` contains your site-specific configuration information. 
      This is mandatory, since there are several configuration values that have no defaults.
      See "Configuration" below.
      * Add `--wait` if you wish the installation command to block until all of the deployed 
      Kubernetes resources are ready, but be aware that this may be waiting for several minutes 
      if anything is mis-configured.   
   * `helm test <release-name> --namespace <namespace>`
      * This will run some basic smoke tests against the deployed release.
      * If any of these tests fail, it is likely that the deployment will not work correctly.
   * Deploy an ingress resource as appropriate for your Kubernetes cluster. 
   See "Ingress" below.   
   
# Configuration

## Ingress
Once the Helm chart has been installed, a suitable HTTP/HTTPS ingress needs to be 
installed also, in order to make the product available from outside of the Kubernetes
cluster. The standard Kubernetes Ingress resource is not flexible enough for our needs,
so a 3rd-party ingress controller and resource definition must be provided.

The [github repository](https://github.com/atlassian-labs/data-center-helm-charts/tree/master/src/test/config)
contains example ingress definitions for the Contour and nginx ingress controllers.
The exact details of the ingress resource will be highly site-specific, but these
examples can be used as a guide.

At a minimum, the ingress needs to support the ability to support long request timeouts, as
well as session affinity (aka "sticky sessions").

## Chart values
Each product's chart contains a large number of configurable options, most 
of which are optional, but a few of which are mandatory. These values can all be specified 
in your `values.yaml`, and which you pass to the `helm install` command.

The mandatory values are one for which there no sensible defaults, and
must be provided by the installer:

* [all products] `database.url` is the JDBC URL of the database that should be used by the product.
   * JDBC URLs vary depending on the JDBC driver and database being used, but for
   postgres an example would be `jdbc:postgresql://host:port/database`
* [jira and bitbucket] `database.driver` is the Java class name of the JDBC driver to be used
   * The JDBC driver must already be present in the product's installation
* [jira and confluence] `database.type` should be one of the valid values as listed in the chart's README
* [bitbucket] `bitbucket.proxy.fqdn` should be the FQDN of the Bitbucket URL

Each chart's README file documents every configurable value. 

See [CONFIG.md]() for examples of what can be done.
    
# Scaling up Data Center

By default, the Helm charts will provision a `StatefulSet` with 1 `replicaCount` of 1.
The `replicaCount` can be altered at runtime to provision a multi-node 
Data Center cluster, with no further configuration required (although note
that the Ingress must support cookie-based sessin affinity in order for the 
products to work correctly in a multi-node configuration).

It is important to note, however, that both Jira and Confluence must initially
be deployed with a `replicaCount` of 1, then be fully configured via the web interface,
and only once setup is complete can they be scaled up to more replicas.

In the case of Bitbucket, this intermediate step is unecessary, and a BitBucket
deployment can be configure with a >1 `replicaCount` from the start.