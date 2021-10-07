# OpenShift

The Helm charts are vendor agnostic and create objects from standard APIs that [OpenShift](https://www.openshift.com/){.external} fully supports.

However, by default OpenShift will not allow running containers as users specified in the image `Dockerfiles`
or `securityContext.fsGroup` in a statefulset/deployment spec. There are a couple of ways to fix this.

## Attach `anyuid` policies
If possible, attach `anyuid` policy to 2 serviceAccounts. Here's an example for a Bitbucket installation.
Please, note that the service account names vary depending on the Data Center product:

=== "For Bitbucket pods"

    ```shell
    oc adm policy add-scc-to-user anyuid -z bitbucket -n git
    ```

=== "For NFS permission fixer pod"

    ```shell
    oc adm policy add-scc-to-user anyuid -z default -n git
    ```

Typically, the `volumes.sharedHome.persistentVolumeClaim.nfsPermissionFixer` needs to be set to `true` to make volume writable.
It depends on the storage backend though.

## Set no security context

As an alternative, (if letting containers run as pre-defined users is not possible), set `product_name.securityContext` to `{}`.
As a result the container will start as a user with an OpenShift generated ID.
Typically, NFS permission fixer job isn't required when no security context is set.

## OpenShift Routes

The Helm charts do not have templates for OpenShift routes that are commonly used in OpenShift instead of ingresses.
Routes need to be manually created after the charts installation. 