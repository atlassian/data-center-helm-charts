# OpenShift

!!! warning "Support Disclaimer"
    Helm is a Kubernetes package manager that orchestrates the provisioning of applications onto existing Kubernetes infrastructure. The requirements for this infrastructure are described in [Prerequisites](../userguide/PREREQUISITES.md). The Kubernetes cluster remains your responsibility; we do not provide direct support for Kubernetes or the underlying hardware it runs on.

    If you have followed our documentation on how to configure the Helm charts, and you're using correctly created components, we will then provide support if you encounter an error with installation after running the `helm install` command. 

    Read more about [what we support and what we don’t](troubleshooting/SUPPORT_BOUNDARIES.md). 

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

## Disable security context

As an alternative, (if letting containers run as pre-defined users is not possible), set `product_name.securityContextEnabled` to `false`, for example, `confluence.securityContextEnabled: false`
As a result the container will start as a user with an OpenShift generated ID.
Typically, NFS permission fixer job isn't required when no security context is set.

## Permission issues

If a container start without anyuid enabled, applications can't write to `${APPLICATION_HOME}/logs`, `${APPLICATION_HOME}/work` and `${APPLICATION_HOME}/temp`.
If you see in logs that the server fails to start with `permission denied` errors, you may want to declare these directories as runtime volumes. To do so, you need to declare additional volume mounts and additional volumes in `values.yaml`:

```
confluence:
  additionalVolumeMounts:
    - name: tomcat-work
      # this example is for Confluence
      mountPath: /opt/atlassian/confluence/work
volumes:
  additional:
    - name: tomcat-work
      emptyDir: {}
```

## OpenShift Routes

The Helm charts do not have templates for OpenShift routes that are commonly used in OpenShift instead of ingresses.
Routes need to be manually created after the charts installation.
