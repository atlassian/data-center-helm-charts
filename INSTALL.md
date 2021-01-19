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
* Volumes
   * While the Helm charts can be installed without providing any dedicated
   storage, it is *strongly* recommended that Persistent Volumes are provisioned,
   including a shared read-write volume. See the "Volumes" section below.
* Ingress controller
   * Because different Kubernetes clusters use different Ingress configurations,
   the Helm charts provide Nginx ingress templates only. By default, *ingress.create* is set to false, an ingress should be created manually after the chart is installed
   
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
      May be omitted, in which case the chart config default will be used.
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

The charts provide a template for Nginx ingress which include all required annotations and optional TLS configuration.

## Service accounts
By default, the Helm charts will create a `ServiceAccount`. This can be configured with
`imagePullSecrets` if required. For Bitbucket and Confluence, which require access
to the Kubernetes API for Data Center peer discovery to work, the charts will also 
create a `ClusterRole`, and a `ClusterRoleBinding` for the `Serviceccount`.

The creation `ServiceAccount`, `ClusterRole` and `ClusterRoleBinding` can all be disabled
if required, but Confluence and Bitbuket still require a `ServiceAccount` with Kubernetes
API access, so either the namespace's default `ServiceAccount` must have the required
permissions, or the name of the pre-existing `ServiceAccount` must be specified.

## Volumes
The Data Center products make use of filesystem storage. Each DC node has its own "local-home" volume, and all
nodes in the DC cluster share a single "shared-home" volume.

By default, the Helm charts will configure all of these volumes as ephemeral "emptyDir" volumes. This makes it 
possible to install the charts without configuring any volume management, but comes with two big caveats:

* Any data stored in the local-home or shared-home will be lost every time a pod starts. Whilst the data that is
stored in local-home can generally be regenerated (e.g. from the database), this can be very a very expensive process
that sometimes required manual intervention. 
* The shared-home volume will not actually be shared between multiple nodes in the DC cluster. Whilst this may not 
immediately prevent scaling the DC cluster up to multiple nodes, certain critical functionality of the products relies 
on the shared filesystem working as expected.

For these reasons, the default volume configuration of the Helm charts is suitable only for running a single DC node for
evaluation porpoises. Proper volume management needs to be configured in order for the data to survive restarts, and for
multi-node DC clusters to operate correctly.

While you are free to configure your Kubernetes volume management in any way you wish, within the constraints imposed by
the products, the recommended setup is to use Kubernetes PersistentVolumes and PersistentVolumeClaims. The `local-home`
volume requires a PersistentVolume with `ReadWriteOnce (RWO)` capability, and `shared-home` requires a PV with `ReadWriteMany (RWX)`
capability. Typically, this will be a NFS volume provided as part of your infrastructure, but some public-cloud Kubernetes
engines provide their own RWX volumes (e.g. AzureFile, ElasticFileStore). While this entails a higher up-front setup effort, 
it gives the best flexibility.

See [CONFIG.md]() for examples of how to configure the volumes.

# Scaling up Data Center

The Helm charts will provision one `StatefulSet`. In order to scale up or down the cluster `kubectl scale` can be used 
at runtime to provision a multi-node Data Center cluster, with no further configuration required (although note
that the Ingress must support cookie-based session affinity in order for the 
products to work correctly in a multi-node configuration). Here is the syntax for scaling up/down the Data Center cluster:
```
kubectl scale statefulsets <statefulsetset-name> --replicas=n
```

# OpenShift Support

The Helm charts are vendor agnostic and create objects from standard APIs that OpenShift fully supports.

However, by default, OpenShift will not allow running containers as users specified in the image Dockerfiles
or **securityContext.fsGroup** in a statefulset/deployment spec. There are a couple of ways to fix it.

## Attach anyuid policies
If possible, attach anyuid policy to 2 serviceAccounts. Here's an example for a Bitbucket installation.
Please, note that the service account name vary depending on the DC product:

```shell
# for Bitbucket pods
oc adm policy add-scc-to-user anyuid -z bitbucket -n git
# for NFS permission fixer pod
oc adm policy add-scc-to-user anyuid -z default -n git
```
Typically, *volumes.sharedHome.persistentVolumeClaim.nfsPermissionFixer* needs to be set to true to make volume writable.
It depends on the storage backend though.

## Set no security context

As an alternative, (if letting containers run as pre-defined users is not possible), set **product_name.securityContext.enabled** to false.
As a result the container will start as a user with an OpenShift generated ID.
Typically, NFS permission fixer job isn't required when no security context is set.

## OpenShift Routes

The Helm charts do not have templates for OpenShift routes which are commonly used in OpenShift instead of ingresses.
Routes need to be manually created after the charts installation. 
