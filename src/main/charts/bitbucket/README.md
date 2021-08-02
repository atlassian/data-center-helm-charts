# bitbucket

![Version: 0.14.0](https://img.shields.io/badge/Version-0.14.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 7.14.1-jdk11](https://img.shields.io/badge/AppVersion-7.14.1--jdk11-informational?style=flat-square)

A chart for installing Bitbucket Data Center on Kubernetes

For installation please follow [the documentation in the repository](https://github.com/atlassian-labs/data-center-helm-charts/blob/master/README.md).

**Homepage:** <https://www.atlassian.com/software/bitbucket>

## Source Code

* <https://github.com/atlassian-labs/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-server/>

## Requirements

Kubernetes: `>=1.19.x-0`

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Bitbucket pods |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be  mounted into service container. Configuration details below (camelCase is important!): 'name'      - References existing ConfigMap or secret name. 'type'      - 'configMap' or 'secret' 'key'       - The file name. 'mountPath' - The destination directory in a container. VolumeMount and Volumes are added with this name and index position, for example;  custom-config-0, keystore-2 |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Bitbucket pods |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources |
| affinity | object | `{}` | Standard Kubernetes affinities that will be applied to all Bitbucket pods Due to the performance requirements it is highly recommended to run all Bitbucket pods in the same availability zone as your dedicated NFS server. To achieve this, you can define `affinity` and `podAffinity` rules that will place all pods into the same zone, and therefore minimise the real distance between the application pods and the shared storage. More specific documentation can be found in the official Affinity and Anti-affinity documentation:  https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity This is an example on how to ensure the pods are in the same zone as NFS server that is labeled with `role=nfs-server`:   podAffinity:    requiredDuringSchedulingIgnoredDuringExecution:      - labelSelector:          matchExpressions:            - key: role              operator: In              values:                - nfs-server # needs to be the same value as NFS server deployment        topologyKey: topology.kubernetes.io/zone |
| bitbucket.additionalBundledPlugins | list | `[]` | Specifies a list of additional Bitbucket plugins that should be added to the  Bitbucket container. Note plugins installed via this method will appear as  bundled plugins rather than user plugins. An alternative to this method  is to install the plugins via "Manage Apps" in the product system  administration UI. These should be specified in the same manner as the  'additionalLibraries' property. |
| bitbucket.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Bitbucket  container. See https://hub.docker.com/r/atlassian/bitbucket-server for  supported variables. |
| bitbucket.additionalJvmArgs[0] | string | `"-XX:ActiveProcessorCount=2"` | The value defined for ActiveProcessorCount should correspond to that provided  for 'container.requests.cpu'. https://docs.oracle.com/en/java/javase/11/tools/java.html#GUID-3B1CE181-CD30-4178-9602-230B800D4FAE |
| bitbucket.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the  Bitbucket container. Each item in the list should specify the name of the volume  that contains the library, as well as the name of the library file within that  volume's root directory. Optionally, a subDirectory field can be included to  specify which directory in the volume contains the library file. |
| bitbucket.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Bitbucket container. These  can refer to existing volumes, or new volumes can be defined via  'volumes.additional'. |
| bitbucket.clustering.enabled | bool | `false` | Set to 'true' if Data Center clustering should be enabled This will automatically configure cluster peer discovery between cluster nodes. |
| bitbucket.elasticSearch.baseUrl | string | `nil` | The base URL of the external Elasticsearch instance to be used. If this is defined, then Bitbucket will disable its internal Elasticsearch instance. |
| bitbucket.elasticSearch.credentials.passwordSecretKey | string | `"password"` | The key in the the Kubernetes Secret that contains the Elasticsearch password. |
| bitbucket.elasticSearch.credentials.secretName | string | `nil` | The name of the Kubernetes Secret that contains the Elasticsearch credentials. Example of creating a credentials K8s secret below: 'kubectl create secret generic <secret-name> --from-literal=username=<username> \ --from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets |
| bitbucket.elasticSearch.credentials.usernameSecreyKey | string | `"username"` | The key in the the Kubernetes Secret that contains the Elasticsearch username. |
| bitbucket.license.secretKey | string | `"license-key"` | The key in the K8s Secret that contains the Bitbucket license key |
| bitbucket.license.secretName | string | `nil` | The name of the K8s Secret that contains the Bitbucket license key. If specified, then  the license will be automatically populated during Bitbucket setup. Otherwise, it will  need to be provided via the browser after initial startup. An Example of creating  a K8s secret for the license below: 'kubectl create secret generic <secret-name> --from-literal=license-key=<license>  https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets |
| bitbucket.ports.hazelcast | int | `5701` | The port on which the Hazlecast listens for client traffic |
| bitbucket.ports.http | int | `7990` | The port on which the Bitbucket container listens for HTTP traffic |
| bitbucket.ports.ssh | int | `7999` | The port on which the Bitbucket container listens for SSH traffic |
| bitbucket.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Bitbucket pod |
| bitbucket.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Bitbucket pod |
| bitbucket.resources.jvm.maxHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Bitbucket JVM The same value will be used by the Elasticsearch JVM. |
| bitbucket.resources.jvm.minHeap | string | `"512m"` | The minimum amount of heap memory that will be used by the Bitbucket JVM The same value will be used by the Elasticsearch JVM. |
| bitbucket.securityContext.enabled | bool | `true` | Set to 'true' to enable the security context |
| bitbucket.securityContext.gid | string | `"2003"` | The GID used by the Bitbucket docker image |
| bitbucket.service.annotations | object | `{}` | Additional annotations to apply to the Service |
| bitbucket.service.port | int | `80` | The port on which the Bitbucket K8s Service will listen |
| bitbucket.service.type | string | `"ClusterIP"` | The type of K8s service to use for Bitbucket |
| bitbucket.sshService | object | `{"annotations":{},"enabled":false,"port":22,"type":"LoadBalancer"}` | Enable or disable an additional service for exposing SSH for external access. Disable when the SSH service is exposed through the ingress controller, or  enable if the ingress controller does not support TCP. |
| bitbucket.sshService.annotations | object | `{}` | Annotations for the SSH service. Useful if a load balancer controller  needs extra annotations. |
| bitbucket.sshService.enabled | bool | `false` | Set to 'true' if an additional SSH Service should be created  |
| bitbucket.sshService.port | int | `22` | Port to expose the SSH service on. |
| bitbucket.sshService.type | string | `"LoadBalancer"` | SSH Service type |
| bitbucket.sysadminCredentials.displayNameSecretKey | string | `"displayName"` | The key in the Kubernetes Secret that contains the sysadmin display name |
| bitbucket.sysadminCredentials.emailAddressSecretKey | string | `"emailAddress"` | The key in the Kubernetes Secret that contains the sysadmin email address |
| bitbucket.sysadminCredentials.passwordSecretKey | string | `"password"` | The key in the Kubernetes Secret that contains the sysadmin password |
| bitbucket.sysadminCredentials.secretName | string | `nil` | The name of the Kubernetes Secret that contains the Bitbucket sysadmin credentials If specified, then these will be automatically populated during Bitbucket setup. Otherwise, they will need to be provided via the browser after initial startup. |
| bitbucket.sysadminCredentials.usernameSecretKey | string | `"username"` | The key in the Kubernetes Secret that contains the sysadmin username |
| database.credentials.passwordSecretKey | string | `"password"` | The key ('password') in the Secret used to store the database login password |
| database.credentials.secretName | string | `nil` | The name of the K8s Secret that contains the database login credentials. If the secret is specified, then the credentials will be automatically utilised on  Bitbucket startup. If the secret is not provided, then the credentials will need  to be provided via the browser during manual configuration post deployment.   Example of creating a database credentials K8s secret below: 'kubectl create secret generic <secret-name> --from-literal=username=<username> \ --from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets |
| database.credentials.usernameSecretKey | string | `"username"` | The key ('username') in the Secret used to store the database login username |
| database.driver | string | `nil` | The Java class name of the JDBC driver to be used. If not specified, then it will  need to be provided via the browser during manual configuration post deployment. Valid drivers are: - 'org.postgresql.Driver' - 'com.mysql.jdbc.Driver' - 'oracle.jdbc.OracleDriver' - 'com.microsoft.sqlserver.jdbc.SQLServerDriver' https://github.com/atlassian-labs/data-center-helm-charts/blob/master/docs/CONFIGURATION.md#databasedriver |
| database.url | string | `nil` | The jdbc URL of the database. If not specified, then it will need to be provided  via the browser during manual configuration post deployment. Example URLs include: - 'jdbc:postgresql://<dbhost>:5432/<dbname>' - 'jdbc:mysql://<dbhost>/<dbname>' - 'jdbc:sqlserver://<dbhost>:1433;databaseName=<dbname>' - 'jdbc:oracle:thin:@<dbhost>:1521:<SID>' https://github.com/atlassian-labs/data-center-helm-charts/blob/master/docs/CONFIGURATION.md#databasejdbcurl |
| fluentd.command | string | `nil` | The command used to start Fluentd. If not supplied the default command  will be used: "fluentd -c /fluentd/etc/fluent.conf -v" Note: The custom command can be free-form, however pay particular attention to the process that should ultimately be left running in the container. This process should be invoked with 'exec' so that signals are appropriately propagated to it, for instance SIGTERM. An example of how such a command may look is: "<command 1> && <command 2> && exec <primary command>" |
| fluentd.customConfigFile | bool | `false` | Set to 'true' if a custom config (see 'configmap-fluentd.yaml' for default)  should be used for Fluentd. If enabled this config must supplied via the  'fluentdCustomConfig' property below. |
| fluentd.elasticsearch.enabled | bool | `true` | Set to 'true' if Fluentd should send all log events to an Elasticsearch service. |
| fluentd.elasticsearch.hostname | string | `"elasticsearch"` | The hostname of the Elasticsearch service that Fluentd should send logs to. |
| fluentd.enabled | bool | `false` | Set to 'true' if the Fluentd sidecar (DaemonSet) should be added to each pod |
| fluentd.extraVolumes | list | `[]` | Specify custom volumes to be added to Fluentd container (e.g. more log sources) |
| fluentd.fluentdCustomConfig | object | `{}` | Custom fluent.conf file |
| fluentd.imageName | string | `"fluent/fluentd-kubernetes-daemonset:v1.11.5-debian-elasticsearch7-1.2"` | The Fluentd sidecar image |
| image | object | `{"pullPolicy":"IfNotPresent","repository":"atlassian/bitbucket-server","tag":""}` | Image configuration  |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy |
| image.repository | string | `"atlassian/bitbucket-server"` | The Confluence Docker image to use https://hub.docker.com/r/atlassian/bitbucket-server |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress Resource  when NOT using the K8s ingress-nginx controller. |
| ingress.create | bool | `false` | Set to 'true' if an Ingress Resource should be created. This depends on a  pre-provisioned Ingress Controller being available.  |
| ingress.host | string | `nil` | The fully-qualified hostname (FQDN) of the Ingress Resource. Traffic coming in on  this hostname will be routed by the Ingress Resource to the appropriate backend  Service. |
| ingress.https | bool | `true` | Set to 'true' if browser communication with the application should be TLS  (HTTPS) enforced. |
| ingress.maxBodySize | string | `"250m"` | The max body size to allow. Requests exceeding this size will result in an HTTP 413 error being returned to the client. |
| ingress.nginx | bool | `true` | Set to 'true' if the Ingress Resource is to use the K8s 'ingress-nginx'  controller.  https://kubernetes.github.io/ingress-nginx/ This will populate the Ingress Resource with annotations that are specific to  the K8s ingress-nginx controller. Set to 'false' if a different controller is  to be used, in which case the appropriate annotations for that controller must  be specified below under 'ingress.annotations'. |
| ingress.path | string | `"/"` | The base path for the Ingress Resource. For example '/bitbucket'. Based on a  'ingress.host' value of 'company.k8s.com' this would result in a URL of  'company.k8s.com/bitbucket' |
| ingress.tlsSecretName | string | `nil` | The name of the K8s Secret that contains the TLS private key and corresponding  certificate. When utilised, TLS termination occurs at the ingress point where  traffic to the Service and it's Pods is in plaintext.  Usage is optional and depends on your use case. The Ingress Controller itself  can also be configured with a TLS secret for all Ingress Resources. https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets https://kubernetes.io/docs/concepts/services-networking/ingress/#tls |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Bitbucket pods |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Bitbucket pods |
| replicaCount | int | `1` | The initial number of Bitbucket pods that should be started at deployment time.  Note that if Bitbucket is fully configured (see above) during initial deployment  a 'replicaCount' greater than 1 can be supplied.  |
| serviceAccount.clusterRole.create | bool | `true` | Set to 'true' if a ClusterRole should be created, or 'false' if it  already exists. |
| serviceAccount.clusterRole.name | string | `nil` | The name of the ClusterRole to be used. If not specified, but  the "serviceAccount.clusterRole.create" flag is set to 'true',  then the ClusterRole name will be auto-generated. |
| serviceAccount.clusterRoleBinding.create | bool | `true` | Set to 'true' if a ClusterRoleBinding should be created, or 'false' if it  already exists. |
| serviceAccount.clusterRoleBinding.name | string | `nil` | The name of the ClusterRoleBinding to be created. If not specified, but  the "serviceAccount.clusterRoleBinding.create" flag is set to 'true',  then the ClusterRoleBinding name will be auto-generated. |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it  already exists. |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull  secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but  the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name  will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Bitbucket pods |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Bitbucket pods. Note that this will not create any corresponding volume mounts; those need to be defined in bitbucket.additionalVolumeMounts |
| volumes.localHome.customVolume | object | `{}` | Static provisioning of local-home using K8s PVs and PVCs NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for  pods is not recommended. Dymamic provisioning described above is the prescribed  approach. When 'persistentVolumeClaim.create' is 'false', then this value can be used to define  a standard K8s volume that will be used for the local-home volume(s). If not defined,  then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify  the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/bitbucket"` | Specifies the path in the Bitbucket container to which the local-home volume will be  mounted. |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically  created for each pod based on the 'StorageClassName' supplied below.    |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the local-home  volume claims. |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the local-home  volume claim.             |
| volumes.sharedHome.customVolume | object | `{}` | Static provisioning of shared-home using K8s PVs and PVCs When 'persistentVolumeClaim.create' is 'false', then this value can be used to define  a standard K8s volume that will be used for the shared-home volume. If not defined,  then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify  the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static https://github.com/atlassian-labs/data-center-helm-charts/blob/master/docs/examples/storage/aws/SHARED_STORAGE.md |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Bitbucket container to which the shared-home volume will be  mounted. |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory  to match the Bitbucket container's GID (2003), and then ensures the directory is  group-writeable. If this is not the desired behaviour, command used can be specified  here. |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `false` | If 'true', this will alter the shared-home volume's root directory so that Bitbucket  can write to it. This is a workaround for a K8s bug affecting NFS volumes:  https://github.com/kubernetes/examples/issues/260 |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the K8s initContainer where the shared-home volume will be mounted |
| volumes.sharedHome.persistentVolume.create | bool | `false` | If 'true', then a 'PersistentVolumeClaim' and 'PersistentVolume' will be dynamically  created for shared-home based on the 'StorageClassName' supplied below. |
| volumes.sharedHome.persistentVolume.mountOptions | list | `[]` | Addtional options used when mounting the NFS volume  |
| volumes.sharedHome.persistentVolume.nfs.path | string | `""` | Specifies the path exported by the NFS server, used in the mount command |
| volumes.sharedHome.persistentVolume.nfs.server | string | `""` | The address of the NFS server. It needs to be resolveable by the kubelet,  so consider using an IP address. |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolumeClaim' and 'PersistentVolume' will be dynamically  created for shared-home based on the 'StorageClassName' supplied below. |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the shared-home  |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the 'shared-home'  |
| volumes.sharedHome.persistentVolumeClaim.volumeName | string | `nil` | Specifies the name of the persistent volume to claim |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume that will be mounted in to the  Bitbucket container. |