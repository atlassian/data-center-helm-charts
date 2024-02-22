# Deploy To OpenShift

!!! warning "Support disclaimer"
    Helm is a Kubernetes package manager that orchestrates the provisioning of applications onto existing Kubernetes infrastructure. The requirements for this infrastructure are described in [Prerequisites](../../userguide/PREREQUISITES.md). The Kubernetes cluster remains your responsibility; we do not provide direct support for Kubernetes or the underlying hardware it runs on.

    If you have followed our documentation on how to configure Helm charts, and you're correctly-created components, we will provide support if you encounter an error with installation after running the `helm install` command.

    Read more about [what we support and what we don’t](../../troubleshooting/SUPPORT_BOUNDARIES.md).

!!! info "Official OpenShift support"
    **Starting from version 1.18, Atlassian Data Center Helm charts officially include support for OpenShift**.

## :simple-redhatopenshift: Deploy with a restricted and nonroot security context constraint

Atlassian Data Center Helm charts are vendor and cloud agnostic, and create objects from standard APIs that [OpenShift](https://www.openshift.com/){.external} fully supports.

However, the default OpenShift **restricted** Security Context Constrain ([SCC](https://docs.openshift.com/container-platform/4.14/authentication/managing-security-context-constraints.html){.external}) requires pods to be run with a UID and a SELinux context that are allocated to the namespace. It's therefore not possible to deploy DC Helm charts with the default settings (with pre-defined `securityContext` and containers that run as root). See [FAQs](../OPENSHIFT_FAQ) for typical errors.

!!! warning "ATL_TOMCAT_* environment variables"
    When running as restricted or nonroot SCC, some of `ATL_TOMCAT_*` environment variables passed to the Helm chart in `additionalEnvironmentVariables` will be ignored, because `server.xml` and  `seraph-config.xml` are mounted as a ConfigMaps rather than generated in the container entrypoint from Jinja templates. Both `<product>.tomcatConfig` and `<product>.seraphConfig` have a number of properties which you can override if necessary. Look at `tomcatConfig` stanza in the chart's values.yaml for more details.

=== "Restricted SCC (:octicons-thumbsup-16: Recommended)"
    !!! info "Restricted SCC"        
        Restricted SCC denies access to all host features and requires pods to be run with a UID and a SELinux context that are allocated to the namespace.
    
    Running containers in OpenShift with a restricted SCC is the most secure approach. To be able to successfully deploy Atlassian DC Helm charts with a restricted SCC, set `openshift.runWithRestrictedSCC` to true in the Helm values file:

    ```yaml
    openshift:
      runWithRestrictedSCC: true
    ```

    This property will:
    
    * unset `securityContext` on the pod and `initContainers` level, letting OpenShift set it
    * trigger the creation of a ConfigMap with `server.xml` and `seraph-config.xml` that are mounted as read-only files into containers (rather than generated in the container entrypoint). Both `server.xml` and `seraph-config.xml` can be configured in `<product>.tomcatConfig` Helm stanza (except for Bitbucket which does not have these configuration files)
    * disable the nfs-permission fixer init container that is run as root by default. If you need to change permissions in an existing shared-home volume, do it when provisioning/migrating the volume. See [FAQs](../OPENSHIFT_FAQ/#nfs-permission-fixer-is-disabled-how-can-i-make-shared-home-writable) for more details.

=== "Nonroot SCC"
    !!! info "Nonroot SCC"
          Nonroot SCC provides all features of the restricted SCC but allows users to run with **any** non-root UID.

    !!! info "Supported versions"
        OpenShift nonroot SCC friendly values are available in versions `1.14.0+` for Jira and Confluence, and `1.18+` for Crowd and Bamboo. If you can't upgrade, consider using 'additionalFiles' to mount `server.xml` and `seraph-config.xml` as ConfigMaps, which should be created outside the Helm chart.

    To be able to run DC containers as users created during an image build you need to let the ServiceAccount use the nonroot SCC. Typically, cluster admin privileges are required (replace namespace and Helm release name):
     
    ```bash
    oc adm policy add-scc-to-user nonroot \
           system:serviceaccount:<namespace>:<helm-release-name>
    ```
    
    Use the following Helm values (replace `jira` and `UIDs` depending on the deployed product):
    
    ```yaml
    jira:
      securityContext:
        runAsUser: 2001
        runAsGroup: 2001
        fsGroup: 2001
      tomcatConfig:
        generateByHelm: true
      seraphConfig:
        generateByHelm: true
    volumes:
      sharedHome:
        nfsPermissionFixer:
          enabled: false  
    ```

    If you enabled `monitoring.exposeJmxMetrics`, you need to run the init container as non root:
    
    ```yaml
    monitoring:
      jmxExporterInitContainer:
        runAsRoot: false
    ```

## :material-transit-connection-variant: OpenShift Routes

To create [OpenShift Routes](https://docs.openshift.com/container-platform/4.14/networking/routes/route-configuration.html){.external} use the following Helm values:

```yaml
ingress:
  create: true
  openShiftRoute: true
  host: your.hostname.com
  path: /yourpath # optional
```

Additional Route configuration include `annotations` and `routeHttpHeaders`:

```yaml
ingress:
  annotations: {}
  routeHttpHeaders: {}
```
Ingress values such as `maxBodySize`, `proxyConnectTimeout`, `proxyReadTimeout`, `proxySendTimeout`, `nginx`, and `className` are ignored when `ingress.openShiftRoute` is set to true.
See [FAQs](../OPENSHIFT_FAQ) for additional information on how to configure OpenShift routes.