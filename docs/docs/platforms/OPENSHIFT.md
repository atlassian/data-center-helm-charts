# OpenShift

!!! warning "Support Disclaimer"
    Helm is a Kubernetes package manager that orchestrates the provisioning of applications onto existing Kubernetes infrastructure. The requirements for this infrastructure are described in [Prerequisites](../userguide/PREREQUISITES.md). The Kubernetes cluster remains your responsibility; we do not provide direct support for Kubernetes or the underlying hardware it runs on.

    If you have followed our documentation on how to configure the Helm charts, and you're using correctly created components, we will then provide support if you encounter an error with installation after running the `helm install` command.

    Read more about [what we support and what we don’t](../troubleshooting/SUPPORT_BOUNDARIES.md).

## Why It Doesn't Work Out-of-the-Box on OpenShift

The Helm charts are vendor agnostic and create objects from standard APIs that [OpenShift](https://www.openshift.com/){.external} fully supports.

However, by default OpenShift will not allow running containers as users specified in the image `Dockerfiles` (which is root in case of Data Center images). Also, it is forbidden to set container's `securityContext` (which OpenShift does automatically). You will see the following error in `StatefulSet` events if you deploy any DC product with default Helm chart values:

```
create Pod jira-0 in StatefulSet jira failed error: pods "jira-0" is forbidden: unable to validate against any security context constraint: [provider "anyuid": Forbidden: not usable by user or serviceaccount, provider restricted: .spec.securityContext.fsGroup: Invalid value: []int64{2001}: 2001 is not an allowed group, spec.initContainers[0].securityContext.runAsUser: Invalid value: 0: must be in the ranges: [1003170000, 1003179999]
```

While it is possible to disable `securityContext`, there are further issues with generating config files (which happens in the container entrypoint):

```
INFO:root:Generating /etc/container_id from template container_id.j2
WARNING:root:Permission problem writing '/etc/container_id'; skipping
INFO:root:Generating /opt/atlassian/jira/conf/server.xml from template server.xml.j2
WARNING:root:Permission problem writing '/opt/atlassian/jira/conf/server.xml'; skipping
INFO:root:Generating /opt/atlassian/jira/atlassian-jira/WEB-INF/classes/seraph-config.xml from template seraph-config.xml.j2
WARNING:root:Permission problem writing '/opt/atlassian/jira/atlassian-jira/WEB-INF/classes/seraph-config.xml'; skipping
INFO:root:Generating /var/atlassian/application-data/jira/dbconfig.xml from template dbconfig.xml.j2
WARNING:root:Could not chown path /var/atlassian/application-data/jira/dbconfig.xml to jira:jira due to insufficient permissions.
INFO:root:Running Jira with command '/opt/atlassian/jira/bin/start-jira.sh', arguments ['/opt/atlassian/jira/bin/start-jira.sh', '-fg']
executing as current user
```

While these are non-fatal errors and Jira/Confluence is able to proceed with startup, failure to properly generate configuration files like `server.xml` will result in a number of runtime errors.

## OpenShift (Run as NonRoot) Friendly Values

!!! info "Supported in 1.14.0+ for Jira and Confluence Only"
    OpenShift friendly values are available since **1.14.0**. If you can't upgrade, consider using `additionalFiles` to mount `server.xml`
    and `seraph-config.xml` as ConfigMaps which should be created outside the Helm chart.

Use the following values (in addition to any custom values) to mitigate the above mentioned permissions and security issues:

```yaml
confluence:

  # securityContext is set by OpenShift automatically
  # so we need to exclude it from the template
  securityContextEnabled: false

  # Tomcat's server.xml will be mounted as a ConfigMap
  # rather than generated in the container entrypoint
  tomcatConfig:
    generateByHelm: true

  # seraph-config.xml will be mounted as a ConfigMap
  # rather than generated in the container entrypoint
  seraphConfig:
    generateByHelm: true

volumes:
  sharedHome:
    # nfs-permission-fixer container is run as root which isn't configurable
    # so we need to disable this init container
    # you need to make sure that the shared-home is writable
    # for the unprivileged user in the container.
    # Typically CSI driver or kubelet will take care of it
    nfsPermissionFixer:
      enabled: false
```

!!! warning "ATL_TOMCAT_* Environment Variables"
    `ATL_TOMCAT_*` environment variables passed to the Helm chart in `additionalEnvironmentVariables` will be ignored, because `server.xml` is mounted as a ConfigMap rather than generated in the container entrypoint from a Jinja template. Both `tomcatConfig` and `seraphConfig` have a number of properties which you can override if necessary. Look at `tomcatConfig` stanza in the chart's values.yaml for more details.

## OpenShift Routes

The Helm charts do not have templates for OpenShift routes that are commonly used in OpenShift instead of ingresses.
Routes need to be manually created after the charts installation.
