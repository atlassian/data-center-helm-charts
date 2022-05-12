# jira

![Version: 1.3.0](https://img.shields.io/badge/Version-1.3.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 8.20.7](https://img.shields.io/badge/AppVersion-8.20.7-informational?style=flat-square)

A chart for installing Jira Data Center on Kubernetes

For installation please follow [the documentation](https://atlassian.github.io/data-center-helm-charts/).

**Homepage:** <https://atlassian.github.io/data-center-helm-charts/>

## Source Code

* <https://github.com/atlassian/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-atlassian-jira/>

## Requirements

Kubernetes: `>=1.19.x-0`

| Repository | Name | Version |
|------------|------|---------|
| https://atlassian.github.io/data-center-helm-charts | common | 1.0.0 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Jira pods |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into service container. Configuration details below (camelCase is important!): 'name'      - References existing ConfigMap or secret name. 'type'      - 'configMap' or 'secret' 'key'       - The file name. 'mountPath' - The destination directory in a container. VolumeMount and Volumes are added with this name and index position, for example; custom-config-0, keystore-2 |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Jira pods |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources |
| affinity | object | `{}` | Standard K8s affinities that will be applied to all Jira pods |
| database.credentials.passwordSecretKey | string | `"password"` | The key ('password') in the Secret used to store the database login password |
| database.credentials.secretName | string | `nil` | The name of the K8s Secret that contains the database login credentials. If the secret is specified, then the credentials will be automatically utilised on Jira startup. If the secret is not provided, then the credentials will need to be provided via the browser during manual configuration post deployment. Example of creating a database credentials K8s secret below: 'kubectl create secret generic <secret-name> --from-literal=username=<username> \ --from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets |
| database.credentials.usernameSecretKey | string | `"username"` | The key ('username') in the Secret used to store the database login username |
| database.driver | string | `nil` | The Java class name of the JDBC driver to be used. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Valid drivers are: - 'org.postgresql.Driver' - 'com.mysql.jdbc.Driver' - 'oracle.jdbc.OracleDriver' - 'com.microsoft.sqlserver.jdbc.SQLServerDriver' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databasedriver: |
| database.type | string | `nil` | The database type that should be used. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Valid values include: - 'postgres72' - 'mysql57' - 'mysql8' - 'oracle10g' - 'mssql' - 'postgresaurora96' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databasetype |
| database.url | string | `nil` | The jdbc URL of the database. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Example URLs include: - 'jdbc:postgresql://<dbhost>:5432/<dbname>' - 'jdbc:mysql://<dbhost>/<dbname>' - 'jdbc:sqlserver://<dbhost>:1433;databaseName=<dbname>' - 'jdbc:oracle:thin:@<dbhost>:1521:<SID>' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databaseurl |
| fluentd.command | string | `nil` | The command used to start Fluentd. If not supplied the default command will be used: "fluentd -c /fluentd/etc/fluent.conf -v" Note: The custom command can be free-form, however pay particular attention to the process that should ultimately be left running in the container. This process should be invoked with 'exec' so that signals are appropriately propagated to it, for instance SIGTERM. An example of how such a command may look is: "<command 1> && <command 2> && exec <primary command>" |
| fluentd.customConfigFile | bool | `false` | Set to 'true' if a custom config (see 'configmap-fluentd.yaml' for default) should be used for Fluentd. If enabled this config must be supplied via the 'fluentdCustomConfig' property below. |
| fluentd.elasticsearch.enabled | bool | `true` | Set to 'true' if Fluentd should send all log events to an Elasticsearch service. |
| fluentd.elasticsearch.hostname | string | `"elasticsearch"` | The hostname of the Elasticsearch service that Fluentd should send logs to. |
| fluentd.elasticsearch.indexNamePrefix | string | `"jira"` | The prefix of the Elasticsearch index name that will be used |
| fluentd.enabled | bool | `false` | Set to 'true' if the Fluentd sidecar (DaemonSet) should be added to each pod |
| fluentd.extraVolumes | list | `[]` | Specify custom volumes to be added to Fluentd container (e.g. more log sources) |
| fluentd.fluentdCustomConfig | object | `{}` | Custom fluent.conf file |
| fluentd.httpPort | int | `9880` | The port on which the Fluentd sidecar will listen |
| fluentd.imageName | string | `"fluent/fluentd-kubernetes-daemonset:v1.11.5-debian-elasticsearch7-1.2"` | The Fluentd sidecar image |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy |
| image.repository | string | `"atlassian/jira-software"` | The Jira Docker image to use https://hub.docker.com/r/atlassian/jira-software |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress Resource when NOT using the K8s ingress-nginx controller. |
| ingress.className | string | `"nginx"` | The class name used by the ingress controller if it's being used. Please follow documentation of your ingress controller. If the cluster contains multiple ingress controllers, this setting allows you to control which of them is used for Atlassian application traffic. |
| ingress.create | bool | `false` | Set to 'true' if an Ingress Resource should be created. This depends on a pre-provisioned Ingress Controller being available. |
| ingress.host | string | `nil` | The fully-qualified hostname (FQDN) of the Ingress Resource. Traffic coming in on this hostname will be routed by the Ingress Resource to the appropriate backend Service. |
| ingress.https | bool | `true` | Set to 'true' if browser communication with the application should be TLS (HTTPS) enforced. |
| ingress.maxBodySize | string | `"250m"` | The max body size to allow. Requests exceeding this size will result in an HTTP 413 error being returned to the client. |
| ingress.nginx | bool | `true` | Set to 'true' if the Ingress Resource is to use the K8s 'ingress-nginx' controller. https://kubernetes.github.io/ingress-nginx/ This will populate the Ingress Resource with annotations that are specific to the K8s ingress-nginx controller. Set to 'false' if a different controller is to be used, in which case the appropriate annotations for that controller must be specified below under 'ingress.annotations'. |
| ingress.path | string | `nil` | The base path for the Ingress Resource. For example '/jira'. Based on a 'ingress.host' value of 'company.k8s.com' this would result in a URL of 'company.k8s.com/jira'. Default value is 'jira.service.contextPath' |
| ingress.tlsSecretName | string | `nil` | The name of the K8s Secret that contains the TLS private key and corresponding certificate. When utilised, TLS termination occurs at the ingress point where traffic to the Service, and it's Pods is in plaintext. Usage is optional and depends on your use case. The Ingress Controller itself can also be configured with a TLS secret for all Ingress Resources. https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets https://kubernetes.io/docs/concepts/services-networking/ingress/#tls |
| jira.accessLog.localHomeSubPath | string | `"log"` | The subdirectory within the local-home volume where access logs should be stored. |
| jira.accessLog.mountPath | string | `"/opt/atlassian/jira/logs"` | The path within the Jira container where the local-home volume should be mounted in order to capture access logs. |
| jira.additionalBundledPlugins | list | `[]` | Specifies a list of additional Jira plugins that should be added to the Jira container. Note plugins installed via this method will appear as bundled plugins rather than user plugins. These should be specified in the same manner as the 'additionalLibraries' property. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/ NOTE: only .jar files can be loaded using this approach. OBR's can be extracted (unzipped) to access the associated .jar An alternative to this method is to install the plugins via "Manage Apps" in the product system administration UI. |
| jira.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Jira container. See https://hub.docker.com/r/atlassian/jira-software for supported variables. |
| jira.additionalJvmArgs | list | `[]` |  |
| jira.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Jira container. Each item in the list should specify the name of the volume that contains the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/ |
| jira.additionalPorts | list | `[]` | Defines any additional ports for the Jira container. |
| jira.additionalVolumeClaimTemplates | list | `[]` | Defines additional volumeClaimTemplates that should be applied to the Jira pod. Note that this will not create any corresponding volume mounts; those needs to be defined in jira.additionalVolumeMounts |
| jira.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Jira container. These can refer to existing volumes, or new volumes can be defined via 'volumes.additional'. |
| jira.clustering.enabled | bool | `false` | Set to 'true' if Data Center clustering should be enabled This will automatically configure cluster peer discovery between cluster nodes. |
| jira.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/ |
| jira.ports.ehcache | int | `40001` | Ehcache port |
| jira.ports.ehcacheobject | int | `40011` | Ehcache object port |
| jira.ports.http | int | `8080` | The port on which the Jira container listens for HTTP traffic |
| jira.readinessProbe.failureThreshold | int | `30` | The number of consecutive failures of the Jira container readiness probe before the pod fails readiness checks. |
| jira.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Jira container readiness probe, after which the probe will start running. |
| jira.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Jira container readiness probe will run |
| jira.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Jira pod |
| jira.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Jira pod |
| jira.resources.jvm.maxHeap | string | `"768m"` | The maximum amount of heap memory that will be used by the Jira JVM |
| jira.resources.jvm.minHeap | string | `"384m"` | The minimum amount of heap memory that will be used by the Jira JVM |
| jira.resources.jvm.reservedCodeCache | string | `"512m"` | The memory reserved for the Jira JVM code cache |
| jira.securityContext.fsGroup | int | `2001` | The GID used by the Jira docker image If not supplied, will default to 2001 This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Jira container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260 |
| jira.service.annotations | object | `{}` | Additional annotations to apply to the Service |
| jira.service.contextPath | string | `nil` | The Tomcat context path that Jira will use. The ATL_TOMCAT_CONTEXTPATH will be set automatically. |
| jira.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer. |
| jira.service.port | int | `80` | The port on which the Jira K8s Service will listen |
| jira.service.type | string | `"ClusterIP"` | The type of K8s service to use for Jira |
| jira.setPermissions | bool | `true` | Boolean to define whether to set local home directory permissions on startup of Jira container. Set to 'false' to disable this behaviour. |
| jira.shutdown.command | string | `"/shutdown-wait.sh"` | By default pods will be stopped via a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), using a script supplied by the Docker image. If any other shutdown behaviour is needed it can be achieved by overriding this value. Note that the shutdown command needs to wait for the application shutdown completely before exiting; see [the default command](https://bitbucket.org/atlassian-docker/docker-atlassian-jira/src/master/shutdown-wait.sh) for details. |
| jira.shutdown.terminationGracePeriodSeconds | int | `30` | The termination grace period for pods during shutdown. This should be set to the internal grace period, plus a small buffer to allow the JVM to fully terminate. |
| jira.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Jira pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/ |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Jira pods |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Jira pods |
| podLabels | object | `{}` | Custom labels that will be applied to all Jira pods |
| replicaCount | int | `1` | The initial number of Jira pods that should be started at deployment time. Note that Jira requires manual configuration via the browser post deployment after the first pod is deployed. This configuration must be completed before scaling up additional pods. As such this value should always be kept as 1, but can be altered once manual configuration is complete. |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Jira pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount (if created) |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it already exists. |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Jira pods |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Jira pods. Note that this will not create any corresponding volume mounts; those needs to be defined in jira.additionalVolumeMounts |
| volumes.localHome.customVolume | object | `{}` | Static provisioning of local-home using K8s PVs and PVCs NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for pods is not recommended. Dynamic provisioning described above is the prescribed approach. When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the local-home volume(s). If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/jira"` | Specifies the path in the Jira container to which the local-home volume will be mounted. |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically created for each pod based on the 'StorageClassName' supplied below. |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the local-home volume claims. |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the local-home volume claim. |
| volumes.sharedHome.customVolume | object | `{}` | Static provisioning of shared-home using K8s PVs and PVCs When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the shared-home volume. If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static https://atlassian.github.io/data-center-helm-charts/examples/storage/aws/SHARED_STORAGE/ |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Jira container to which the shared-home volume will be mounted. |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Jira container's GID (2001), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here. |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `true` | If 'true', this will alter the shared-home volume's root directory so that Jira can write to it. This is a workaround for a K8s bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260 |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the K8s initContainer where the shared-home volume will be mounted |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolumeClaim' and 'PersistentVolume' will be dynamically created for shared-home based on the 'StorageClassName' supplied below. |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the shared-home volume claims. |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the 'shared-home' |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume that will be mounted in to the Jira container. |