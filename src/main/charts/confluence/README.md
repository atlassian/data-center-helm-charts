# confluence

![Version: 1.3.0](https://img.shields.io/badge/Version-1.3.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 7.13.5](https://img.shields.io/badge/AppVersion-7.13.5-informational?style=flat-square)

A chart for installing Confluence Data Center on Kubernetes

For installation please follow [the documentation](https://atlassian.github.io/data-center-helm-charts/).

**Homepage:** <https://atlassian.github.io/data-center-helm-charts/>

## Source Code

* <https://github.com/atlassian/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/>

## Requirements

Kubernetes: `>=1.19.x-0`

| Repository | Name | Version |
|------------|------|---------|
| https://atlassian.github.io/data-center-helm-charts | common | 1.0.0 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Confluence pods |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into service container. Configuration details below (camelCase is important!): 'name'      - References existing ConfigMap or secret name. 'type'      - 'configMap' or 'secret' 'key'       - The file name. 'mountPath' - The destination directory in a container. VolumeMount and Volumes are added with this name and index position, for example; custom-config-0, keystore-2 |
| additionalHosts | list | `[]` | Additional host aliases for each pod, equivalent to adding them to the /etc/hosts file. https://kubernetes.io/docs/concepts/services-networking/add-entries-to-pod-etc-hosts-with-host-aliases/ |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Confluence pods |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources |
| affinity | object | `{}` | Standard K8s affinities that will be applied to all Confluence pods |
| confluence.accessLog.enabled | bool | `true` | Set to 'true' if access logging should be enabled. |
| confluence.accessLog.localHomeSubPath | string | `"logs"` | The subdirectory within the local-home volume where access logs should be stored. |
| confluence.accessLog.mountPath | string | `"/opt/atlassian/confluence/logs"` | The path within the Confluence container where the local-home volume should be mounted in order to capture access logs. |
| confluence.additionalBundledPlugins | list | `[]` | Specifies a list of additional Confluence plugins that should be added to the Confluence container. Note plugins installed via this method will appear as bundled plugins rather than user plugins. These should be specified in the same manner as the 'additionalLibraries' property. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/ NOTE: only .jar files can be loaded using this approach. OBR's can be extracted (unzipped) to access the associated .jar An alternative to this method is to install the plugins via "Manage Apps" in the product system administration UI. |
| confluence.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Confluence container. See https://hub.docker.com/r/atlassian/confluence-server for supported variables. |
| confluence.additionalJvmArgs | list | `[]` | Specifies a list of additional arguments that can be passed to the Confluence JVM, e.g. system properties. |
| confluence.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Confluence container. Each item in the list should specify the name of the volume that contains the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/ |
| confluence.additionalPorts | list | `[]` | Defines any additional ports for the Confluence container. |
| confluence.additionalVolumeClaimTemplates | list | `[]` | Defines additional volumeClaimTemplates that should be applied to the Confluence pod. Note that this will not create any corresponding volume mounts; those needs to be defined in confluence.additionalVolumeMounts |
| confluence.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Confluence container. These can refer to existing volumes, or new volumes can be defined via 'volumes.additional'. |
| confluence.clustering.enabled | bool | `false` | Set to 'true' if Data Center clustering should be enabled This will automatically configure cluster peer discovery between cluster nodes. |
| confluence.clustering.usePodNameAsClusterNodeName | bool | `true` | Set to 'true' if the K8s pod name should be used as the end-user-visible name of the Data Center cluster node. |
| confluence.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/ |
| confluence.jvmDebug.enabled | bool | `false` | Set to 'true' for remote debugging. Confluence JVM will be started with debugging port 5005 open. |
| confluence.license.secretKey | string | `"license-key"` | The key in the K8s Secret that contains the Confluence license key |
| confluence.license.secretName | string | `nil` | The name of the K8s Secret that contains the Confluence license key. If specified, then the license will be automatically populated during Confluence setup. Otherwise, it will need to be provided via the browser after initial startup. An Example of creating a K8s secret for the license below: 'kubectl create secret generic <secret-name> --from-literal=license-key=<license> https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets |
| confluence.ports.hazelcast | int | `5701` | The port on which the Confluence container listens for Hazelcast traffic |
| confluence.ports.http | int | `8090` | The port on which the Confluence container listens for HTTP traffic |
| confluence.readinessProbe.failureThreshold | int | `6` | The number of consecutive failures of the Confluence container readiness probe before the pod fails readiness checks. |
| confluence.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Confluence container readiness probe, after which the probe will start running. |
| confluence.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Confluence container readiness probe will run |
| confluence.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Confluence pod. |
| confluence.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Confluence pod |
| confluence.resources.jvm.maxHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Confluence JVM |
| confluence.resources.jvm.minHeap | string | `"1g"` | The minimum amount of heap memory that will be used by the Confluence JVM |
| confluence.resources.jvm.reservedCodeCache | string | `"256m"` | The memory reserved for the Confluence JVM code cache |
| confluence.securityContext.fsGroup | int | `2002` | The GID used by the Confluence docker image If not supplied, will default to 2002 This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Confluence container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260 |
| confluence.service.annotations | object | `{}` | Additional annotations to apply to the Service |
| confluence.service.contextPath | string | `nil` | The Tomcat context path that Confluence will use. The ATL_TOMCAT_CONTEXTPATH will be set automatically. |
| confluence.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer. |
| confluence.service.port | int | `80` | The port on which the Confluence K8s Service will listen |
| confluence.service.type | string | `"ClusterIP"` | The type of K8s service to use for Confluence |
| confluence.setPermissions | bool | `true` | Boolean to define whether to set local home directory permissions on startup of Confluence container. Set to 'false' to disable this behaviour. |
| confluence.shutdown.command | string | `"/shutdown-wait.sh"` | By default pods will be stopped via a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), using a script supplied by the Docker image. If any other shutdown behaviour is needed it can be achieved by overriding this value. Note that the shutdown command needs to wait for the application shutdown completely before exiting; see [the default command](https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/src/master/shutdown-wait.sh) for details. |
| confluence.shutdown.terminationGracePeriodSeconds | int | `25` | The termination grace period for pods during shutdown. This should be set to the Confluence internal grace period (default 20 seconds), plus a small buffer to allow the JVM to fully terminate. |
| confluence.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Confluence pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/ |
| confluence.umask | string | `"0022"` | The umask used by the Confluence process when it creates new files. The default is 0022. This gives the new files:  - read/write permissions for the Confluence user  - read permissions for everyone else. |
| database.credentials.passwordSecretKey | string | `"password"` | The key ('password') in the Secret used to store the database login password |
| database.credentials.secretName | string | `nil` | The name of the K8s Secret that contains the database login credentials. If the secret is specified, then the credentials will be automatically utilised on Confluence startup. If the secret is not provided, then the credentials will need to be provided via the browser during manual configuration post deployment. Example of creating a database credentials K8s secret below: 'kubectl create secret generic <secret-name> --from-literal=username=<username> \ --from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets |
| database.credentials.usernameSecretKey | string | `"username"` | The key ('username') in the Secret used to store the database login username |
| database.type | string | `nil` | The database type that should be used. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Valid values include: - 'postgresql' - 'mysql' - 'oracle' - 'mssql' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databasetype |
| database.url | string | `nil` | The jdbc URL of the database. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Example URLs include: - 'jdbc:postgresql://<dbhost>:5432/<dbname>' - 'jdbc:mysql://<dbhost>/<dbname>' - 'jdbc:sqlserver://<dbhost>:1433;databaseName=<dbname>' - 'jdbc:oracle:thin:@<dbhost>:1521:<SID>' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databaseurl |
| fluentd.command | string | `nil` | The command used to start Fluentd. If not supplied the default command will be used: "fluentd -c /fluentd/etc/fluent.conf -v" Note: The custom command can be free-form, however pay particular attention to the process that should ultimately be left running in the container. This process should be invoked with 'exec' so that signals are appropriately propagated to it, for instance SIGTERM. An example of how such a command may look is: "<command 1> && <command 2> && exec <primary command>" |
| fluentd.customConfigFile | bool | `false` | Set to 'true' if a custom config (see 'configmap-fluentd.yaml' for default) should be used for Fluentd. If enabled this config must be supplied via the 'fluentdCustomConfig' property below. |
| fluentd.elasticsearch.enabled | bool | `true` | Set to 'true' if Fluentd should send all log events to an Elasticsearch service. |
| fluentd.elasticsearch.hostname | string | `"elasticsearch"` | The hostname of the Elasticsearch service that Fluentd should send logs to. |
| fluentd.elasticsearch.indexNamePrefix | string | `"confluence"` | The prefix of the Elasticsearch index name that will be used |
| fluentd.enabled | bool | `false` | Set to 'true' if the Fluentd sidecar (DaemonSet) should be added to each pod |
| fluentd.extraVolumes | list | `[]` | Specify custom volumes to be added to Fluentd container (e.g. more log sources) |
| fluentd.fluentdCustomConfig | object | `{}` | Custom fluent.conf file |
| fluentd.httpPort | int | `9880` | The port on which the Fluentd sidecar will listen |
| fluentd.imageName | string | `"fluent/fluentd-kubernetes-daemonset:v1.11.5-debian-elasticsearch7-1.2"` | The Fluentd sidecar image |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy |
| image.repository | string | `"atlassian/confluence"` | The Confluence Docker image to use https://hub.docker.com/r/atlassian/confluence-server |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress Resource when NOT using the K8s ingress-nginx controller. |
| ingress.className | string | `"nginx"` | The class name used by the ingress controller if it's being used. Please follow documentation of your ingress controller. If the cluster contains multiple ingress controllers, this setting allows you to control which of them is used for Atlassian application traffic. |
| ingress.create | bool | `false` | Set to 'true' if an Ingress Resource should be created. This depends on a pre-provisioned Ingress Controller being available. |
| ingress.host | string | `nil` | The fully-qualified hostname (FQDN) of the Ingress Resource. Traffic coming in on this hostname will be routed by the Ingress Resource to the appropriate backend Service. |
| ingress.https | bool | `true` | Set to 'true' if browser communication with the application should be TLS (HTTPS) enforced. |
| ingress.maxBodySize | string | `"250m"` | The max body size to allow. Requests exceeding this size will result in an HTTP 413 error being returned to the client. |
| ingress.nginx | bool | `true` | Set to 'true' if the Ingress Resource is to use the K8s 'ingress-nginx' controller. https://kubernetes.github.io/ingress-nginx/ This will populate the Ingress Resource with annotations that are specific to the K8s ingress-nginx controller. Set to 'false' if a different controller is to be used, in which case the appropriate annotations for that controller must be specified below under 'ingress.annotations'. |
| ingress.path | string | `nil` | The base path for the Ingress Resource. For example '/confluence'. Based on a 'ingress.host' value of 'company.k8s.com' this would result in a URL of 'company.k8s.com/confluence'. Default value is 'confluence.service.contextPath' |
| ingress.tlsSecretName | string | `nil` | The name of the K8s Secret that contains the TLS private key and corresponding certificate. When utilised, TLS termination occurs at the ingress point where traffic to the Service, and it's Pods is in plaintext. Usage is optional and depends on your use case. The Ingress Controller itself can also be configured with a TLS secret for all Ingress Resources. https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets https://kubernetes.io/docs/concepts/services-networking/ingress/#tls |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Confluence pods |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Confluence pods |
| podLabels | object | `{}` | Custom labels that will be applied to all Confluence pods |
| replicaCount | int | `1` | The initial number of Confluence pods that should be started at deployment time. Note that Confluence requires manual configuration via the browser post deployment after the first pod is deployed. This configuration must be completed before scaling up additional pods. As such this value should always be kept as 1, but can be altered once manual configuration is complete. |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Confluence pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount (if created) |
| serviceAccount.clusterRole.create | bool | `true` | Set to 'true' if a ClusterRole should be created, or 'false' if it already exists. |
| serviceAccount.clusterRole.name | string | `nil` | The name of the ClusterRole to be used. If not specified, but the "serviceAccount.clusterRole.create" flag is set to 'true', then the ClusterRole name will be auto-generated. |
| serviceAccount.clusterRoleBinding.create | bool | `true` | Set to 'true' if a ClusterRoleBinding should be created, or 'false' if it already exists. |
| serviceAccount.clusterRoleBinding.name | string | `nil` | The name of the ClusterRoleBinding to be created. If not specified, but the "serviceAccount.clusterRoleBinding.create" flag is set to 'true', then the ClusterRoleBinding name will be auto-generated. |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it already exists. |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server |
| synchrony.additionalJvmArgs | object | `{}` | Specifies a list of additional arguments that can be passed to the Synchrony JVM, e.g. system properties. |
| synchrony.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Synchrony container. Each item in the list should specify the name of the volume that contains the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/ |
| synchrony.additionalPorts | list | `[]` | Defines any additional ports for the Synchrony container. |
| synchrony.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Synchrony container. These can refer to existing volumes, or new volumes can be defined via 'volumes.additionalSynchrony'. |
| synchrony.enabled | bool | `false` | Set to 'true' if Synchrony (i.e. collaborative editing) should be enabled. This will result in a separate StatefulSet and Service to be created for Synchrony. If disabled, then collaborative editing will be disabled in Confluence. |
| synchrony.ingressUrl | string | `nil` | The base URL of the Synchrony service. This will be the URL that users' browsers will be given to communicate with Synchrony, as well as the URL that the Confluence service will use to communicate directly with Synchrony, so the URL must be resolvable both from inside and outside the Kubernetes cluster. |
| synchrony.ports.hazelcast | int | `5701` | The port on which the Synchrony container listens for Hazelcast traffic |
| synchrony.ports.http | int | `8091` | The port on which the Synchrony container listens for HTTP traffic |
| synchrony.readinessProbe.failureThreshold | int | `10` | The number of consecutive failures of the Synchrony container readiness probe before the pod fails readiness checks. |
| synchrony.readinessProbe.initialDelaySeconds | int | `5` | The initial delay (in seconds) for the Synchrony container readiness probe, after which the probe will start running. |
| synchrony.readinessProbe.periodSeconds | int | `1` | How often (in seconds) the Synchrony container readiness probe will run |
| synchrony.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Synchrony pod |
| synchrony.resources.container.requests.memory | string | `"2.5G"` | Initial Memory request Synchrony pod |
| synchrony.resources.jvm.maxHeap | string | `"2g"` | The minimum amount of heap memory that will be used by the Synchrony JVM |
| synchrony.resources.jvm.minHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Synchrony JVM |
| synchrony.resources.jvm.stackSize | string | `"2048k"` | The memory allocated for the Synchrony stack |
| synchrony.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer. |
| synchrony.service.port | int | `80` | The port on which the Synchrony K8s Service will listen |
| synchrony.service.type | string | `"ClusterIP"` | The type of K8s service to use for Synchrony |
| synchrony.setPermissions | bool | `true` | Boolean to define whether to set synchrony home directory permissions on startup of Synchrony container. Set to 'false' to disable this behaviour. |
| synchrony.shutdown.terminationGracePeriodSeconds | int | `25` | The termination grace period for pods during shutdown. This should be set to the Synchrony internal grace period (default 20 seconds), plus a small buffer to allow the JVM to fully terminate. |
| synchrony.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Synchrony pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/ |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Confluence pods |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Confluence pods. Note that this will not create any corresponding volume mounts; those needs to be defined in confluence.additionalVolumeMounts |
| volumes.additionalSynchrony | list | `[]` | Defines additional volumes that should be applied to all Synchrony pods. Note that this will not create any corresponding volume mounts; those needs to be defined in synchrony.additionalVolumeMounts |
| volumes.localHome.customVolume | object | `{}` | Static provisioning of local-home using K8s PVs and PVCs NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for pods is not recommended. Dynamic provisioning described above is the prescribed approach. When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the local-home volume(s). If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/confluence"` | Specifies the path in the Confluence container to which the local-home volume will be mounted. |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically created for each pod based on the 'StorageClassName' supplied below. |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests for the local-home volume claims. |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the local-home volume claim. |
| volumes.sharedHome.customVolume | object | `{}` | Static provisioning of shared-home using K8s PVs and PVCs When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the shared-home volume. If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static https://atlassian.github.io/data-center-helm-charts/examples/storage/aws/SHARED_STORAGE/ |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Confluence container to which the shared-home volume will be mounted. |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Confluence container's GID (2002), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here. |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `true` | If 'true', this will alter the shared-home volume's root directory so that Confluence can write to it. This is a workaround for a K8s bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260 |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the K8s initContainer where the shared-home volume will be mounted |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolumeClaim' and 'PersistentVolume' will be dynamically created for shared-home based on the 'StorageClassName' supplied below. |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests limits for the shared-home volume claims. |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the 'shared-home' volume claim. |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume that will be mounted in to the Confluence container. |
| volumes.synchronyHome.customVolume | object | `{}` | Static provisioning of synchrony-home using K8s PVs and PVCs NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for pods is not recommended. Dynamic provisioning described above is the prescribed approach. When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the synchrony-home volume(s). If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static |
| volumes.synchronyHome.mountPath | string | `"/var/atlassian/application-data/confluence"` | Specifies the path in the Synchrony container to which the synchrony-home volume will be mounted. |
| volumes.synchronyHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically created for each pod based on the 'StorageClassName' supplied below. |
| volumes.synchronyHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests for the synchrony-home volume claims. |
| volumes.synchronyHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the synchrony-home volume claim. |