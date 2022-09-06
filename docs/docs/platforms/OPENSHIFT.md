# OpenShift

The Helm charts are vendor agnostic and create objects from standard APIs that [OpenShift](https://www.openshift.com/){.external} fully supports.

However, by default OpenShift will not allow running containers as users specified in the image `Dockerfiles`
or `securityContext.fsGroup` in a statefulset/deployment spec. You will see the following error in stateful set events if you deploy with default Helm chart values:

```
create Pod jira-0 in StatefulSet jira failed error: pods "jira-0" is forbidden: unable to validate against any security context constraint: [provider "anyuid": Forbidden: not usable by user or serviceaccount, provider restricted: .spec.securityContext.fsGroup: Invalid value: []int64{2001}: 2001 is not an allowed group, spec.initContainers[0].securityContext.runAsUser: Invalid value: 0: must be in the ranges: [1003170000, 1003179999]
```

There are a couple of ways to fix this.

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
You will also need to disable NFS permission fixer init container as it start as root. Set `volumes.sharedHome.nfsPermissionFixer.enabled=false` to false.

## Permission issues

If a container starts without `anyuid` enabled, applications can't write to `${APPLICATION_HOME}/logs`, `${APPLICATION_HOME}/work` and `${APPLICATION_HOME}/temp`.
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

While it's possible to declare runtime volumes for empty directories, it is not possible for `${APPLICATION_HOME}/conf`. When starting up, Jira and Confluence generate a few configuration files which is a part of the image entrypoint. Without `anyuid` SCC, an unprivileged user can't write to `${APPLICATION_HOME}/conf`. When starting Jira in OpenShift without `anyuid` SCC attached to jira service account, you will see the following log:

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

While this is a non-fatal error and Jira is able to proceed with startup, failure to properly generate configuration files like server.xml will result in a number of errors when using Jira.

To mitigate the problem, either attach anyuid policy to jira (or confluence) service account **or** build your own container image if existing security practices do not allow anyuid. You need to inherit the Jira (or Confluence) official image and make a few directories/files writable for users belonging to a `root` group (which users in OpenShfit containers belong to):

```
FROM atlassian/jira-software:$JIRA_VERSION
RUN chgrp -R 0 /opt/atlassian/jira/conf && chmod -R g=u /opt/atlassian/jira/conf && \
    chgrp 0 /etc/container_id && chmod g=u /etc/container_id
```

## OpenShift Routes

The Helm charts do not have templates for OpenShift routes that are commonly used in OpenShift instead of ingresses.
Routes need to be manually created after the charts installation.
