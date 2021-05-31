# Platforms
The platforms documented on this page **are not officially supported** as part of the Atlassian Data Center products, and should be used only as examples.

## OpenShift Support

The Helm charts are vendor agnostic and create objects from standard APIs that OpenShift fully supports.

However, by default OpenShift will not allow running containers as users specified in the image Dockerfiles
or **securityContext.fsGroup** in a statefulset/deployment spec. There are a couple of ways to fix it.

### Attach anyuid policies
If possible, attach anyuid policy to 2 serviceAccounts. Here's an example for a Bitbucket installation.
Please, note that the service account names vary depending on the Data Center product:

```shell
# for Bitbucket pods
oc adm policy add-scc-to-user anyuid -z bitbucket -n git
# for NFS permission fixer pod
oc adm policy add-scc-to-user anyuid -z default -n git
```
Typically, *volumes.sharedHome.persistentVolumeClaim.nfsPermissionFixer* needs to be set to true to make volume writable.
It depends on the storage backend though.

### Set no security context

As an alternative, (if letting containers run as pre-defined users is not possible), set **product_name.securityContext.enabled** to false.
As a result the container will start as a user with an OpenShift generated ID.
Typically, NFS permission fixer job isn't required when no security context is set.

### OpenShift Routes

The Helm charts do not have templates for OpenShift routes that are commonly used in OpenShift instead of ingresses.
Routes need to be manually created after the charts installation. 

***
* Go back to the [installation guide](INSTALLATION.md)
* Dive deeper into the [configuration](CONFIGURATION.md) options
* Go back to [README.md](../README.md)
