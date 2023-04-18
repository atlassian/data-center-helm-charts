# bitbucket

![Version: 1.12.0](https://img.shields.io/badge/Version-1.12.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 8.9.0](https://img.shields.io/badge/AppVersion-8.9.0-informational?style=flat-square)

A chart for installing Bitbucket Data Center on Kubernetes

**Homepage:** <https://atlassian.github.io/data-center-helm-charts/>

## Source Code

* <https://github.com/atlassian/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-server/>

## Requirements

Kubernetes: `>=1.21.x-0`

| Repository | Name | Version |
|------------|------|---------|
| https://atlassian.github.io/data-center-helm-charts | common | 1.0.0 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Bitbucket pods  |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into service container. Configuration details below (camelCase is important!): 'name'      - References existing ConfigMap or secret name. 'type'      - 'configMap' or 'secret' 'key'       - The file name. 'mountPath' - The destination directory in a container. VolumeMount and Volumes are added with this name and index position, for example; custom-config-0, keystore-2  |
| additionalHosts | list | `[]` | Additional host aliases for each pod, equivalent to adding them to the /etc/hosts file. https://kubernetes.io/docs/concepts/services-networking/add-entries-to-pod-etc-hosts-with-host-aliases/ |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Bitbucket pods  |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources  |
| affinity | object | `{}` | Standard Kubernetes affinities that will be applied to all Bitbucket pods Due to the performance requirements it is highly recommended running all Bitbucket pods in the same availability zone as your dedicated NFS server. To achieve this, you can define `affinity` and `podAffinity` rules that will place all pods into the same zone, and therefore minimise the real distance between the application pods and the shared storage. More specific documentation can be found in the official Affinity and Anti-affinity documentation:  https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity  This is an example on how to ensure the pods are in the same zone as NFS server that is labeled with `role=nfs-server`:    podAffinity:    requiredDuringSchedulingIgnoredDuringExecution:      - labelSelector:          matchExpressions:            - key: role              operator: In              values:                - nfs-server # needs to be the same value as NFS server deployment        topologyKey: topology.kubernetes.io/zone |
| bitbucket.additionalBundledPlugins | list | `[]` | Specifies a list of additional Bitbucket plugins that should be added to the Bitbucket container. Note plugins installed via this method will appear as bundled plugins rather than user plugins. These should be specified in the same manner as the 'additionalLibraries' property. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/  NOTE: only .jar files can be loaded using this approach. OBR's can be extracted (unzipped) to access the associated .jar  An alternative to this method is to install the plugins via "Manage Apps" in the product system administration UI.  |
| bitbucket.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Bitbucket container. See https://hub.docker.com/r/atlassian/bitbucket-server for supported variables.  |
| bitbucket.additionalJvmArgs | list | `[]` | Specifies a list of additional arguments that can be passed to the Bitbucket JVM, e.g. system properties.  |
| bitbucket.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Bitbucket container. Each item in the list should specify the name of the volume that contains the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/  |
| bitbucket.additionalPorts | list | `[]` | Defines any additional ports for the Bitbucket container.  |
| bitbucket.additionalVolumeClaimTemplates | list | `[]` | Defines additional volumeClaimTemplates that should be applied to the Bitbucket pod. Note that this will not create any corresponding volume mounts; those needs to be defined in bitbucket.additionalVolumeMounts  |
| bitbucket.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Bitbucket container. These can refer to existing volumes, or new volumes can be defined via 'volumes.additional'.  |
| bitbucket.applicationMode | string | `"default"` | Application Mode  This can be either 'default' or 'mirror'  |
| bitbucket.clustering.enabled | bool | `false` | Set to 'true' if Data Center clustering should be enabled This will automatically configure cluster peer discovery between cluster nodes.  |
| bitbucket.clustering.group.nameSecretKey | string | `"name"` | The key in the Kubernetes Secret that contains the Hazelcast group name.  |
| bitbucket.clustering.group.passwordSecretKey | string | `"password"` | The key in the Kubernetes Secret that contains the Hazelcast group password.  |
| bitbucket.clustering.group.secretName | string | `nil` | from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets  If no secret is specified, a default group name will be used and a random password will be generated during installation.  |
| bitbucket.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/  |
| bitbucket.displayName | string | `nil` | Set the display name of the Bitbucket instance. Note that this value is only used during installation and changing the value during an upgrade has no effect.  |
| bitbucket.elasticSearch.baseUrl | string | `nil` | The base URL of the external Elasticsearch instance to be used, for example: http://elasticsearch-master.<namespace>.svc.cluster.local:9200 If this is defined, then Bitbucket will disable its internal Elasticsearch instance.  |
| bitbucket.elasticSearch.credentials.passwordSecretKey | string | `"password"` | The key in the Kubernetes Secret that contains the Elasticsearch password.  |
| bitbucket.elasticSearch.credentials.secretName | string | `nil` | from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets  |
| bitbucket.elasticSearch.credentials.usernameSecretKey | string | `"username"` | The key in the Kubernetes Secret that contains the Elasticsearch username.  |
| bitbucket.license.secretKey | string | `"license-key"` | The key in the K8s Secret that contains the Bitbucket license key  |
| bitbucket.license.secretName | string | `nil` | The name of the K8s Secret that contains the Bitbucket license key. If specified, then the license will be automatically populated during Bitbucket setup. Otherwise, it will need to be provided via the browser after initial startup. An Example of creating a K8s secret for the license below: 'kubectl create secret generic <secret-name> --from-literal=license-key=<license> https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets  |
| bitbucket.mesh.additionalEnvironmentVariables | object | `{}` | Defines any additional environment variables to be passed to the Bitbucket mesh containers.  |
| bitbucket.mesh.additionalFiles | string | `nil` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into service container  |
| bitbucket.mesh.additionalInitContainers | object | `{}` | Additional initContainer definitions that will be added to all Bitbucket pods  |
| bitbucket.mesh.additionalJvmArgs | list | `[]` | Specifies a list of additional arguments that can be passed to the Bitbucket Mesh JVM, e.g. system properties.  |
| bitbucket.mesh.affinity | object | `{}` | Standard Kubernetes affinities that will be applied to all Bitbucket mesh pods  |
| bitbucket.mesh.enabled | bool | `false` | Enable Bitbucket Mesh. See: https://confluence.atlassian.com/bitbucketserver/bitbucket-mesh-1128304351.html  |
| bitbucket.mesh.image | object | `{"pullPolicy":"IfNotPresent","repository":"atlassian/bitbucket-mesh","tag":"1.5.0"}` | The Bitbucket Mesh image to use https://hub.docker.com/r/atlassian/bitbucket-mesh  |
| bitbucket.mesh.image.pullPolicy | string | `"IfNotPresent"` | Image pull policy  |
| bitbucket.mesh.image.repository | string | `"atlassian/bitbucket-mesh"` | The Bitbucket Mesh image repository https://hub.docker.com/r/atlassian/bitbucket-mesh  |
| bitbucket.mesh.image.tag | string | `"1.5.0"` | The docker image tag to be used  |
| bitbucket.mesh.nodeAutoRegistration | bool | `false` | Experimental! Automatically register Bitbucket mesh nodes with the Bitbucket server. `bitbucket.sysadminCredentials.secretName` needs to be defined to provide credentials to post-install node registration jobs that are created only for new Helm chart installations. It is recommended to manually register Mesh nodes in Butbucket UI.  |
| bitbucket.mesh.nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Bitbucket Mesh pods  |
| bitbucket.mesh.podAnnotations | object | `{}` | Custom annotations that will be applied to all Bitbucket Mesh pods  |
| bitbucket.mesh.podLabels | object | `{}` | Custom labels that will be applied to all Bitbucket Mesh pods  |
| bitbucket.mesh.podManagementPolicy | string | `"OrderedReady"` |  |
| bitbucket.mesh.priorityClassName | string | `nil` | Pod PriorityClassName https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/#priorityclass  |
| bitbucket.mesh.replicaCount | int | `3` | Number of Bitbucket Mesh nodes. Do not change it. Currently, only the quorum of 3 mesh nodes is supported. Reducing the number of replicas will result in mesh degradation while increasing the number of Mesh nodes will result in new nodes being unused by the Bitbucket server.  |
| bitbucket.mesh.resources | object | `{"container":{"limits":{"cpu":"2","memory":"2G"},"requests":{"cpu":"1","memory":"2G"}},"jvm":{"maxHeap":"1g","minHeap":"512m"}}` | Bitbucket Mesh resources requests and limits  |
| bitbucket.mesh.resources.container | object | `{"limits":{"cpu":"2","memory":"2G"},"requests":{"cpu":"1","memory":"2G"}}` | Bitbucket Mesh container cpu/mem requests and limits  |
| bitbucket.mesh.resources.jvm | object | `{"maxHeap":"1g","minHeap":"512m"}` | Bitbucket Mesh JVM heap settings  |
| bitbucket.mesh.schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Bitbucket pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods  |
| bitbucket.mesh.service.annotations | object | `{}` | Bitbucket mesh service annotations  |
| bitbucket.mesh.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer.  |
| bitbucket.mesh.service.port | int | `7777` | Bitbucket Mesh port  |
| bitbucket.mesh.service.type | string | `"ClusterIP"` | The type of K8s service to use for Bitbucket mesh service  |
| bitbucket.mesh.setByDefault | bool | `false` | Experimental! Automatically create all new repositories on Bitbucket mesh nodes. `bitbucket.sysadminCredentials.secretName` needs to be defined to provide credentials to node post-install job. It is recommended to manually configure it in Bitbucket UI.  |
| bitbucket.mesh.shutdown.terminationGracePeriodSeconds | int | `35` | The termination grace period for pods during shutdown. This should be set to the Bitbucket internal grace period (default 30 seconds), plus a small buffer to allow the JVM to fully terminate.  |
| bitbucket.mesh.tolerations | object | `{}` | Standard K8s tolerations that will be applied to all Bitbucket Mesh pods  |
| bitbucket.mesh.topologySpreadConstraints | object | `{}` | Defines topology spread constraints for Bitbucket Mesh pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/  |
| bitbucket.mesh.volume | object | `{"create":true,"mountPath":"/var/atlassian/application-data/mesh","resources":{"requests":{"storage":"1Gi"}},"storageClass":null}` | Mesh home volume settings. Disabling persistence results in data loss!  |
| bitbucket.mirror.upstreamUrl | string | `nil` | Specifies the URL of the upstream Bitbucket server for this mirror.  |
| bitbucket.podManagementStrategy | string | `"OrderedReady"` | Pod management strategy. Bitbucket Data Center requires the "OrderedReady" value but for Bitbucket Mirrors you can use the "Parallel" option. To learn more, visit https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/#pod-management-policies  |
| bitbucket.ports.hazelcast | int | `5701` | The port on which the Hazelcast listens for client traffic  |
| bitbucket.ports.http | int | `7990` | The port on which the Bitbucket container listens for HTTP traffic  |
| bitbucket.ports.ssh | int | `7999` | The port on which the Bitbucket container listens for SSH traffic  |
| bitbucket.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Bitbucket pod  |
| bitbucket.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Bitbucket pod  |
| bitbucket.resources.jvm.maxHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Bitbucket JVM The same value will be used by the Elasticsearch JVM. |
| bitbucket.resources.jvm.minHeap | string | `"512m"` | The minimum amount of heap memory that will be used by the Bitbucket JVM The same value will be used by the Elasticsearch JVM. |
| bitbucket.securityContext.fsGroup | int | `2003` | The GID used by the Bitbucket docker image GID will default to 2003 if not supplied and securityContextEnabled is set to true. This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Bitbucket container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260  |
| bitbucket.securityContextEnabled | bool | `true` | Whether to apply security context to pod.  |
| bitbucket.service.annotations | object | `{}` | Additional annotations to apply to the Service  |
| bitbucket.service.contextPath | string | `nil` | The context path that Bitbucket will use.  |
| bitbucket.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer.  |
| bitbucket.service.port | int | `80` | The port on which the Bitbucket K8s Service will listen  |
| bitbucket.service.type | string | `"ClusterIP"` | The type of K8s service to use for Bitbucket  |
| bitbucket.setPermissions | bool | `true` | Boolean to define whether to set local home directory permissions on startup of Bitbucket container. Set to 'false' to disable this behaviour.  |
| bitbucket.shutdown.command | string | `"/shutdown-wait.sh"` | By default pods will be stopped via a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), using a script supplied by the Docker image. If any other shutdown behaviour is needed it can be achieved by overriding this value. Note that the shutdown command needs to wait for the application shutdown completely before exiting; see [the default command](https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-server/src/master/shutdown-wait.sh) for details.  |
| bitbucket.shutdown.terminationGracePeriodSeconds | int | `35` | The termination grace period for pods during shutdown. This should be set to the Bitbucket internal grace period (default 30 seconds), plus a small buffer to allow the JVM to fully terminate.  |
| bitbucket.sshService | object | `{"annotations":{},"enabled":false,"host":null,"loadBalancerIP":null,"port":22,"type":"LoadBalancer"}` | Enable or disable an additional service for exposing SSH for external access. Disable when the SSH service is exposed through the ingress controller, or enable if the ingress controller does not support TCP.  |
| bitbucket.sshService.annotations | object | `{}` | Annotations for the SSH service. Useful if a load balancer controller needs extra annotations.  |
| bitbucket.sshService.enabled | bool | `false` | Set to 'true' if an additional SSH Service should be created  |
| bitbucket.sshService.host | string | `nil` | The hostname of the SSH service. If set, it'll be used to configure the SSH base URL for the application.  |
| bitbucket.sshService.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer.  |
| bitbucket.sshService.port | int | `22` | Port to expose the SSH service on.  |
| bitbucket.sshService.type | string | `"LoadBalancer"` | SSH Service type  |
| bitbucket.sysadminCredentials.displayNameSecretKey | string | `"displayName"` | The key in the Kubernetes Secret that contains the sysadmin display name  |
| bitbucket.sysadminCredentials.emailAddressSecretKey | string | `"emailAddress"` | The key in the Kubernetes Secret that contains the sysadmin email address  |
| bitbucket.sysadminCredentials.passwordSecretKey | string | `"password"` | The key in the Kubernetes Secret that contains the sysadmin password  |
| bitbucket.sysadminCredentials.secretName | string | `nil` | The name of the Kubernetes Secret that contains the Bitbucket sysadmin credentials If specified, then these will be automatically populated during Bitbucket setup. Otherwise, they will need to be provided via the browser after initial startup.  |
| bitbucket.sysadminCredentials.usernameSecretKey | string | `"username"` | The key in the Kubernetes Secret that contains the sysadmin username  |
| bitbucket.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Bitbucket pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/  |
| database.credentials.passwordSecretKey | string | `"password"` | The key ('password') in the Secret used to store the database login password  |
| database.credentials.secretName | string | `nil` | from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets  |
| database.credentials.usernameSecretKey | string | `"username"` | The key ('username') in the Secret used to store the database login username  |
| database.driver | string | `nil` | The Java class name of the JDBC driver to be used. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Valid drivers are: - 'org.postgresql.Driver' - 'com.mysql.jdbc.Driver' - 'oracle.jdbc.OracleDriver' - 'com.microsoft.sqlserver.jdbc.SQLServerDriver' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databasedriver:  |
| database.url | string | `nil` | The jdbc URL of the database. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Example URLs include: - 'jdbc:postgresql://<dbhost>:5432/<dbname>' - 'jdbc:mysql://<dbhost>/<dbname>' - 'jdbc:sqlserver://<dbhost>:1433;databaseName=<dbname>' - 'jdbc:oracle:thin:@<dbhost>:1521:<SID>' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databaseurl  |
| fluentd.command | string | `nil` | The command used to start Fluentd. If not supplied the default command will be used: "fluentd -c /fluentd/etc/fluent.conf -v"  Note: The custom command can be free-form, however pay particular attention to the process that should ultimately be left running in the container. This process should be invoked with 'exec' so that signals are appropriately propagated to it, for instance SIGTERM. An example of how such a command may look is: "<command 1> && <command 2> && exec <primary command>" |
| fluentd.customConfigFile | bool | `false` | Set to 'true' if a custom config (see 'configmap-fluentd.yaml' for default) should be used for Fluentd. If enabled this config must be supplied via the 'fluentdCustomConfig' property below.  |
| fluentd.elasticsearch.enabled | bool | `true` | Set to 'true' if Fluentd should send all log events to an Elasticsearch service.  |
| fluentd.elasticsearch.hostname | string | `"elasticsearch"` | The hostname of the Elasticsearch service that Fluentd should send logs to.  |
| fluentd.enabled | bool | `false` | Set to 'true' if the Fluentd sidecar (DaemonSet) should be added to each pod  |
| fluentd.extraVolumes | list | `[]` | Specify custom volumes to be added to Fluentd container (e.g. more log sources)  |
| fluentd.fluentdCustomConfig | object | `{}` | Custom fluent.conf file  |
| fluentd.imageRepo | string | `"fluent/fluentd-kubernetes-daemonset"` | The Fluentd sidecar image repository  |
| fluentd.imageTag | string | `"v1.11.5-debian-elasticsearch7-1.2"` | The Fluentd sidecar image tag  |
| image | object | `{"pullPolicy":"IfNotPresent","repository":"atlassian/bitbucket","tag":""}` | Image configuration  |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy  |
| image.repository | string | `"atlassian/bitbucket"` | The Confluence Docker image to use https://hub.docker.com/r/atlassian/bitbucket-server  |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion  |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress Resource when NOT using the K8s ingress-nginx controller.  |
| ingress.className | string | `"nginx"` | The class name used by the ingress controller if it's being used.  Please follow documentation of your ingress controller. If the cluster contains multiple ingress controllers, this setting allows you to control which of them is used for Atlassian application traffic.  |
| ingress.create | bool | `false` | Set to 'true' if an Ingress Resource should be created. This depends on a pre-provisioned Ingress Controller being available.  |
| ingress.host | string | `nil` | The fully-qualified hostname (FQDN) of the Ingress Resource. Traffic coming in on this hostname will be routed by the Ingress Resource to the appropriate backend Service.  |
| ingress.https | bool | `true` | Set to 'true' if browser communication with the application should be TLS (HTTPS) enforced.  |
| ingress.maxBodySize | string | `"250m"` | The max body size to allow. Requests exceeding this size will result in an HTTP 413 error being returned to the client.  |
| ingress.nginx | bool | `true` | Set to 'true' if the Ingress Resource is to use the K8s 'ingress-nginx' controller. https://kubernetes.github.io/ingress-nginx/  This will populate the Ingress Resource with annotations that are specific to the K8s ingress-nginx controller. Set to 'false' if a different controller is to be used, in which case the appropriate annotations for that controller must be specified below under 'ingress.annotations'.  |
| ingress.path | string | `nil` | The base path for the Ingress Resource. For example '/bitbucket'. Based on a 'ingress.host' value of 'company.k8s.com' this would result in a URL of 'company.k8s.com/bitbucket'. Default value is 'bitbucket.service.contextPath'.  |
| ingress.proxyConnectTimeout | int | `60` | Defines a timeout for establishing a connection with a proxied server. It should be noted that this timeout cannot usually exceed 75 seconds.  |
| ingress.proxyReadTimeout | int | `60` | Defines a timeout for reading a response from the proxied server. The timeout is set only between two successive read operations, not for the transmission of the whole response. If the proxied server does not transmit anything within this time, the connection is closed.  |
| ingress.proxySendTimeout | int | `60` | Sets a timeout for transmitting a request to the proxied server. The timeout is set only between two successive write operations, not for the transmission of the whole request. If the proxied server does not receive anything within this time, the connection is closed.  |
| ingress.tlsSecretName | string | `nil` | The name of the K8s Secret that contains the TLS private key and corresponding certificate. When utilised, TLS termination occurs at the ingress point where traffic to the Service, and it's Pods is in plaintext.  Usage is optional and depends on your use case. The Ingress Controller itself can also be configured with a TLS secret for all Ingress Resources. https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets https://kubernetes.io/docs/concepts/services-networking/ingress/#tls  |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Bitbucket pods  |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Bitbucket pods  |
| podLabels | object | `{}` | Custom labels that will be applied to all Bitbucket pods  |
| priorityClassName | string | `nil` | Priority class for the application pods. The PriorityClass with this name needs to be available in the cluster. For details see https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/#priorityclass  |
| replicaCount | int | `1` | The initial number of Bitbucket pods that should be started at deployment time. Note that if Bitbucket is fully configured (see above) during initial deployment a 'replicaCount' greater than 1 can be supplied.  |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Bitbucket pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods  |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount (if created)  |
| serviceAccount.clusterRole.create | bool | `false` | Set to 'true' if a ClusterRole should be created, or 'false' if it already exists.  |
| serviceAccount.clusterRole.name | string | `nil` | The name of the ClusterRole to be used. If not specified, but the "serviceAccount.clusterRole.create" flag is set to 'true', then the ClusterRole name will be auto-generated.  |
| serviceAccount.clusterRoleBinding.create | bool | `false` | Set to 'true' if a ClusterRoleBinding should be created, or 'false' if it already exists.  |
| serviceAccount.clusterRoleBinding.name | string | `nil` | The name of the ClusterRoleBinding to be created. If not specified, but the "serviceAccount.clusterRoleBinding.create" flag is set to 'true', then the ClusterRoleBinding name will be auto-generated.  |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it already exists.  |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod  |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server  |
| serviceAccount.role.create | bool | `true` | Create a role for Hazelcast client with privileges to get and list pods and endpoints in the namespace. Set to false if you need to create a Role and RoleBinding manually  |
| serviceAccount.roleBinding | object | `{"create":true}` | Grant permissions defined in Role (list and get pods and endpoints) to a service account.  |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Bitbucket pods  |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Bitbucket pods. Note that this will not create any corresponding volume mounts; those need to be defined in bitbucket.additionalVolumeMounts  |
| volumes.localHome.customVolume | object | `{}` | Static provisioning of local-home using K8s PVs and PVCs  NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for pods is not recommended. Dynamic provisioning described above is the prescribed approach.  When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the local-home volume(s). If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static  |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/bitbucket"` | Specifies the path in the Bitbucket container to which the local-home volume will be mounted.  |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically created for each pod based on the 'StorageClassName' supplied below.  |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the local-home volume claims.  |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the local-home volume claim.  |
| volumes.sharedHome.customVolume | object | `{}` | Static provisioning of shared-home using K8s PVs and PVCs  When 'persistentVolume.create' and 'persistentVolumeClaim.create' are 'false', then this property can be used to define a custom volume that will be used for shared-home If not defined, then an 'emptyDir' volume is utilised.  Having manually provisioned a 'PersistentVolume' with corresponding 'PersistentVolumeClaim' specify the bound claim name below https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static https://atlassian.github.io/data-center-helm-charts/examples/storage/aws/SHARED_STORAGE/  |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Bitbucket container to which the shared-home volume will be mounted.  |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Bitbucket container's GID (2003), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here.  |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `true` | If 'true', this will alter the shared-home volume's root directory so that Bitbucket can write to it. This is a workaround for a K8s bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260  |
| volumes.sharedHome.nfsPermissionFixer.imageRepo | string | `"alpine"` | Image repository for the permission fixer init container. Defaults to alpine  |
| volumes.sharedHome.nfsPermissionFixer.imageTag | string | `"latest"` | Image tag for the permission fixer init container. Defaults to latest  |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the K8s initContainer where the shared-home volume will be mounted  |
| volumes.sharedHome.persistentVolume.create | bool | `false` | If 'true' then a 'PersistentVolume' will be created for the NFS server  |
| volumes.sharedHome.persistentVolume.mountOptions | list | `[]` | Additional options to be used when mounting the NFS volume  |
| volumes.sharedHome.persistentVolume.nfs.path | string | `""` | Specifies NFS directory share. This will be mounted into the Pod(s) using the 'volumes.sharedHome.mountPath'  |
| volumes.sharedHome.persistentVolume.nfs.server | string | `""` | The address of the NFS server. It needs to be resolvable by the kubelet, so consider using an IP address.  |
| volumes.sharedHome.persistentVolumeClaim.accessMode | string | `"ReadWriteMany"` | Specifies the access mode of the volume to claim  |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolumeClaim' will be created for the 'PersistentVolume'  |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the shared-home volume claims.  |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used  If set to non-empty string value, this will specify the storage class to be used. If left without value, the default Storage Class will be utilised. Alternatively, can be set to the empty string "", to indicate that no Storage Class should be used here. |
| volumes.sharedHome.persistentVolumeClaim.volumeName | string | `nil` | If persistentVolume.create and persistentVolumeClaim.create are both true then any value supplied here is ignored and the default used. A custom value here is useful when bringing your own 'PersistentVolume' i.e. 'persistentVolume.create' is false.  |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume that will be mounted in to the Bitbucket container.  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.11.0](https://github.com/norwoodj/helm-docs/releases/v1.11.0)
