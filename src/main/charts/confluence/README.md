# confluence

![Version: 1.22.5](https://img.shields.io/badge/Version-1.22.5-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 9.2.1](https://img.shields.io/badge/AppVersion-9.2.1-informational?style=flat-square)

A chart for installing Confluence Data Center on Kubernetes

**Homepage:** <https://atlassian.github.io/data-center-helm-charts/>

## Source Code

* <https://github.com/atlassian/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/>

## Requirements

Kubernetes: `>=1.21.x-0`

| Repository | Name | Version |
|------------|------|---------|
| https://atlassian.github.io/data-center-helm-charts | common | 1.2.7 |
| https://opensearch-project.github.io/helm-charts | opensearch | 2.19.0 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalConfigMaps | list | `[]` | Create additional ConfigMaps with given names, keys and content. Ther Helm release name will be used as a prefix for a ConfigMap name, fileName is used as subPath  |
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Confluence pods  |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into service container. Configuration details below (camelCase is important!): 'name'      - References existing ConfigMap or secret name. 'type'      - 'configMap' or 'secret' 'key'       - The file name. 'mountPath' - The destination directory in a container. VolumeMount and Volumes are added with this name and index position, for example; custom-config-0, keystore-2  |
| additionalHosts | list | `[]` | Additional host aliases for each pod, equivalent to adding them to the /etc/hosts file. https://kubernetes.io/docs/concepts/services-networking/add-entries-to-pod-etc-hosts-with-host-aliases/ |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Confluence pods  |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources  |
| affinity | object | `{}` | Standard K8s affinities that will be applied to all Confluence pods  |
| atlassianAnalyticsAndSupport.analytics.enabled | bool | `true` | Mount ConfigMap with selected Helm chart values as a JSON which DC products will read and send analytics events to Atlassian data pipelines  |
| atlassianAnalyticsAndSupport.helmValues.enabled | bool | `true` | Mount ConfigMap with selected Helm chart values as a YAML file which can be optionally including to support.zip  |
| confluence.accessLog.enabled | bool | `true` | Set to 'true' if access logging should be enabled.  |
| confluence.accessLog.localHomeSubPath | string | `"logs"` | The subdirectory within the local-home volume where access logs should be stored.  |
| confluence.accessLog.mountPath | string | `"/opt/atlassian/confluence/logs"` | The path within the Confluence container where the local-home volume should be mounted in order to capture access logs.  |
| confluence.additionalAnnotations | object | `{}` | Defines additional annotations to the Confluence StateFulSet. This might be required when deploying using a GitOps approach |
| confluence.additionalBundledPlugins | list | `[]` | Specifies a list of additional Confluence plugins that should be added to the Confluence container. Note plugins installed via this method will appear as bundled plugins rather than user plugins. These should be specified in the same manner as the 'additionalLibraries' property. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/  NOTE: only .jar files can be loaded using this approach. OBR's can be extracted (unzipped) to access the associated .jar  An alternative to this method is to install the plugins via "Manage Apps" in the product system administration UI.  |
| confluence.additionalCertificates | object | `{"customCmd":null,"initContainer":{"resources":{},"securityContext":{}},"secretList":[],"secretName":null}` | Certificates to be added to Java truststore. Provide reference to a secret that contains the certificates  |
| confluence.additionalCertificates.customCmd | string | `nil` | Custom command to be executed in the init container to import certificates  |
| confluence.additionalCertificates.initContainer.resources | object | `{}` | Resources allocated to the import-certs init container  |
| confluence.additionalCertificates.initContainer.securityContext | object | `{}` | Custom SecurityContext for the import-certs init container  |
| confluence.additionalCertificates.secretList | list | `[]` | A list of secrets with their respective keys holding certificates to be added to the Java truststore. It is mandatory to specify which keys from secret data need to be mounted as files to the init container.  |
| confluence.additionalCertificates.secretName | string | `nil` | Name of the Kubernetes secret with certificates in its data. All secret keys in the secret data will be treated as certificates to be added to Java truststore. If defined, this takes precedence over secretList.  |
| confluence.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Confluence container. See https://hub.docker.com/r/atlassian/confluence for supported variables.  |
| confluence.additionalJvmArgs | list | `[]` | Specifies a list of additional arguments that can be passed to the Confluence JVM, e.g. system properties.  |
| confluence.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Confluence container. Each item in the list should specify the name of the volume that contains the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/  |
| confluence.additionalPorts | list | `[]` | Defines any additional ports for the Confluence container.  |
| confluence.additionalVolumeClaimTemplates | list | `[]` | Defines additional volumeClaimTemplates that should be applied to the Confluence pod. Note that this will not create any corresponding volume mounts; those needs to be defined in confluence.additionalVolumeMounts  |
| confluence.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Confluence container. These can refer to existing volumes, or new volumes can be defined via 'volumes.additional'.  |
| confluence.clustering.enabled | bool | `false` | Set to 'true' if Data Center clustering should be enabled This will automatically configure cluster peer discovery between cluster nodes.  |
| confluence.clustering.usePodNameAsClusterNodeName | bool | `true` | Set to 'true' if the K8s pod name should be used as the end-user-visible name of the Data Center cluster node.  |
| confluence.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/  |
| confluence.forceConfigUpdate | bool | `false` | The Docker entrypoint.py generates application configuration on first start; not all of these files are regenerated on subsequent starts. By default, confluence.cfg.xml is generated only once. Set `forceConfigUpdate` to true to change this behavior.  |
| confluence.hazelcastService.annotations | object | `{}` | Additional annotations to apply to the Hazelcast Service  |
| confluence.hazelcastService.enabled | bool | `false` | Enable or disable an additional Hazelcast service that Confluence nodes can use to join a cluster. It is recommended to create a separate Hazelcast service if the Confluence service uses a LoadBalancer type (e.g., NLB), ensuring that the Hazelcast port is not exposed at all. |
| confluence.hazelcastService.port | int | `5701` | The port on which the Confluence K8s Hazelcast Service will listen  |
| confluence.hazelcastService.type | string | `"ClusterIP"` | The type of the Hazelcast K8s service to use for Confluence  |
| confluence.jvmDebug.enabled | bool | `false` | Set to 'true' for remote debugging. Confluence JVM will be started with debugging port 5005 open. |
| confluence.license.secretKey | string | `"license-key"` | The key in the K8s Secret that contains the Confluence license key  |
| confluence.license.secretName | string | `nil` | The name of the K8s Secret that contains the Confluence license key. If specified, then the license will be automatically populated during Confluence setup. Otherwise, it will need to be provided via the browser after initial startup. An Example of creating a K8s secret for the license below: 'kubectl create secret generic <secret-name> --from-literal=license-key=<license> https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets  |
| confluence.livenessProbe.customProbe | object | `{}` | Custom livenessProbe to override the default tcpSocket probe  |
| confluence.livenessProbe.enabled | bool | `false` | Whether to apply the livenessProbe check to pod.  |
| confluence.livenessProbe.failureThreshold | int | `12` | The number of consecutive failures of the Confluence container liveness probe before the pod fails liveness checks.  |
| confluence.livenessProbe.initialDelaySeconds | int | `60` | Time to wait before starting the first probe  |
| confluence.livenessProbe.periodSeconds | int | `5` | How often (in seconds) the Confluence container liveness probe will run  |
| confluence.livenessProbe.timeoutSeconds | int | `1` | Number of seconds after which the probe times out  |
| confluence.ports.hazelcast | int | `5701` | The port on which the Confluence container listens for Hazelcast traffic  |
| confluence.ports.http | int | `8090` | The port on which the Confluence container listens for HTTP traffic  |
| confluence.postStart | object | `{"command":null}` | PostStart is executed immediately after a container is created. However, there is no guarantee that the hook will execute before the container ENTRYPOINT. See: https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/#container-hooks  |
| confluence.readinessProbe.customProbe | object | `{}` | Custom readinessProbe to override the default /status httpGet  |
| confluence.readinessProbe.enabled | bool | `true` | Whether to apply the readinessProbe check to pod.  |
| confluence.readinessProbe.failureThreshold | int | `6` | The number of consecutive failures of the Confluence container readiness probe before the pod fails readiness checks.  |
| confluence.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Confluence container readiness probe, after which the probe will start running.  |
| confluence.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Confluence container readiness probe will run  |
| confluence.readinessProbe.timeoutSeconds | int | `1` | Number of seconds after which the probe times out  |
| confluence.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Confluence pod.  |
| confluence.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Confluence pod  |
| confluence.resources.jvm.maxHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Confluence JVM  |
| confluence.resources.jvm.minHeap | string | `"1g"` | The minimum amount of heap memory that will be used by the Confluence JVM  |
| confluence.resources.jvm.reservedCodeCache | string | `"256m"` | The memory reserved for the Confluence JVM code cache  |
| confluence.s3AttachmentsStorage.bucketName | string | `nil` |  |
| confluence.s3AttachmentsStorage.bucketRegion | string | `nil` |  |
| confluence.s3AttachmentsStorage.endpointOverride | string | `nil` | EXPERIMENTAL Feature! Override the default AWS API endpoint with a custom one, for example to use Minio as object storage https://min.io/  |
| confluence.securityContext.fsGroup | int | `2002` | The GID used by the Confluence docker image GID will default to 2002 if not supplied and securityContextEnabled is set to true. This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Confluence container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260 |
| confluence.securityContextEnabled | bool | `true` | Whether to apply security context to pod.  |
| confluence.seraphConfig | object | `{"autoLoginCookieAge":"1209600","generateByHelm":false}` | By default seraph-config.xml is generated in the container entrypoint from a template shipped with an official Confluence image. However, seraph-config.xml generation may fail if container is not run as root, which is a common case if Confluence is deployed to OpenShift.  |
| confluence.seraphConfig.generateByHelm | bool | `false` | Mount seraph-config.xml as a ConfigMap. Override configuration elements if necessary  |
| confluence.service.annotations | object | `{}` | Additional annotations to apply to the Service  |
| confluence.service.contextPath | string | `nil` | The Tomcat context path that Confluence will use. The ATL_TOMCAT_CONTEXTPATH will be set automatically.  |
| confluence.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer.  |
| confluence.service.nodePort | string | `nil` | Only applicable if service.type is NodePort. NodePort for Confluence service  |
| confluence.service.port | int | `80` | The port on which the Confluence K8s Service will listen  |
| confluence.service.sessionAffinity | string | `"None"` | Session affinity type. If you want to make sure that connections from a particular client are passed to the same pod each time, set sessionAffinity to ClientIP. See: https://kubernetes.io/docs/reference/networking/virtual-ips/#session-affinity  |
| confluence.service.sessionAffinityConfig | object | `{"clientIP":{"timeoutSeconds":null}}` | Session affinity configuration  |
| confluence.service.sessionAffinityConfig.clientIP.timeoutSeconds | string | `nil` | Specifies the seconds of ClientIP type session sticky time. The value must be > 0 && <= 86400(for 1 day) if ServiceAffinity == "ClientIP". Default value is 10800 (for 3 hours).  |
| confluence.service.type | string | `"ClusterIP"` | The type of K8s service to use for Confluence  |
| confluence.setPermissions | bool | `true` | Boolean to define whether to set local home directory permissions on startup of Confluence container. Set to 'false' to disable this behaviour.  |
| confluence.shutdown.command | string | `"/shutdown-wait.sh"` | By default pods will be stopped via a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), using a script supplied by the Docker image. If any other shutdown behaviour is needed it can be achieved by overriding this value. Note that the shutdown command needs to wait for the application shutdown completely before exiting; see [the default command](https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/src/master/shutdown-wait.sh) for details.  |
| confluence.shutdown.terminationGracePeriodSeconds | int | `25` | The termination grace period for pods during shutdown. This should be set to the Confluence internal grace period (default 20 seconds), plus a small buffer to allow the JVM to fully terminate.  |
| confluence.startupProbe.enabled | bool | `false` | Whether to apply the startupProbe check to pod.  |
| confluence.startupProbe.failureThreshold | int | `120` | The number of consecutive failures of the Confluence container startup probe before the pod fails startup checks.  |
| confluence.startupProbe.initialDelaySeconds | int | `60` | Time to wait before starting the first probe  |
| confluence.startupProbe.periodSeconds | int | `5` | How often (in seconds) the Confluence container startup probe will run  |
| confluence.tomcatConfig | object | `{"acceptCount":"100","accessLogMaxDays":"-1","accessLogPattern":"%h %{X-AUSERNAME}o %t &quot;%r&quot; %s %b %D %U %I &quot;%{User-Agent}i&quot;","connectionTimeout":"20000","customServerXml":"","debug":"0","enableLookups":"false","generateByHelm":false,"maxHttpHeaderSize":"8192","maxThreads":"100","mgmtPort":"8000","minSpareThreads":"10","port":"8090","protocol":"org.apache.coyote.http11.Http11NioProtocol","proxyInternalIps":null,"proxyName":null,"proxyPort":null,"redirectPort":"8443","requestAttributesEnabled":"false","scheme":null,"secure":null,"stuckThreadDetectionValveThreshold":"60","trustedProxies":null,"uriEncoding":"UTF-8"}` | By default Tomcat's server.xml is generated in the container entrypoint from a template shipped with an official Confluence image. However, server.xml generation may fail if container is not run as root, which is a common case if Confluence is deployed to OpenShift.  |
| confluence.tomcatConfig.customServerXml | string | `""` | Custom server.xml to be mounted into /opt/atlassian/confluence/conf  |
| confluence.tomcatConfig.generateByHelm | bool | `false` | Mount server.xml as a ConfigMap. Override configuration elements if necessary  |
| confluence.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Confluence pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/  |
| confluence.tunnel | object | `{"additionalConnector":{"URIEncoding":"UTF-8","acceptCount":"10","connectionTimeout":"20000","enableLookups":"false","maxThreads":"50","minSpareThreads":"10","port":null,"secure":false}}` | Configure additional Tomcat connector to set up tunnel. Define the connector port and optional additional attributes. When 'tunnel.additionalConnector.port' is defined, an additional connector is added to server.xml and '-Dsecure.tunnel.upstream.port=<port_number>' is added to JVM args  |
| confluence.umask | string | `"0022"` | The umask used by the Confluence process when it creates new files. The default is 0022. This gives the new files:  - read/write permissions for the Confluence user  - read permissions for everyone else.  |
| confluence.useHelmReleaseNameAsContainerName | bool | `false` | Whether the main container should acquire helm release name. By default the container name is `confluence` which corresponds to the name of the Helm Chart.  |
| database.credentials.passwordSecretKey | string | `"password"` | The key ('password') in the Secret used to store the database login password  |
| database.credentials.secretName | string | `nil` | from-literal=password=<password>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets  |
| database.credentials.usernameSecretKey | string | `"username"` | The key ('username') in the Secret used to store the database login username  |
| database.type | string | `nil` | The database type that should be used. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Valid values include: * 'postgresql' * 'mysql' * 'oracle' * 'mssql' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databasetype  |
| database.url | string | `nil` | The jdbc URL of the database. If not specified, then it will need to be provided via the browser during manual configuration post deployment. Example URLs include: * 'jdbc:postgresql://<dbhost>:5432/<dbname>' * 'jdbc:mysql://<dbhost>/<dbname>' * 'jdbc:sqlserver://<dbhost>:1433;databaseName=<dbname>' * 'jdbc:oracle:thin:@<dbhost>:1521:<SID>' https://atlassian.github.io/data-center-helm-charts/userguide/CONFIGURATION/#databaseurl  |
| fluentd.command | string | `nil` | The command used to start Fluentd. If not supplied the default command will be used: "fluentd -c /fluentd/etc/fluent.conf -v"  Note: The custom command can be free-form, however pay particular attention to the process that should ultimately be left running in the container. This process should be invoked with 'exec' so that signals are appropriately propagated to it, for instance SIGTERM. An example of how such a command may look is: "<command 1> && <command 2> && exec <primary command>" |
| fluentd.customConfigFile | bool | `false` | Set to 'true' if a custom config (see 'configmap-fluentd.yaml' for default) should be used for Fluentd. If enabled this config must be supplied via the 'fluentdCustomConfig' property below. If your custom config forces fluentd to run in a server mode, add `-Datlassian.logging.cloud.enabled=true` to `confluence.AdditionalJvmArgs` stanza in values file  |
| fluentd.elasticsearch.enabled | bool | `true` | Set to 'true' if Fluentd should send all log events to an Elasticsearch service.  |
| fluentd.elasticsearch.hostname | string | `"elasticsearch"` | The hostname of the Elasticsearch service that Fluentd should send logs to.  |
| fluentd.elasticsearch.indexNamePrefix | string | `"confluence"` | The prefix of the Elasticsearch index name that will be used  |
| fluentd.enabled | bool | `false` | Set to 'true' if the Fluentd sidecar (DaemonSet) should be added to each pod  |
| fluentd.extraVolumes | list | `[]` | Specify custom volumes to be added to Fluentd container (e.g. more log sources)  |
| fluentd.fluentdCustomConfig | object | `{}` | Custom fluent.conf file  |
| fluentd.httpPort | int | `9880` | The port on which the Fluentd sidecar will listen  |
| fluentd.imageRepo | string | `"fluent/fluentd-kubernetes-daemonset"` | The Fluentd sidecar image repository  |
| fluentd.imageTag | string | `"v1.11.5-debian-elasticsearch7-1.2"` | The Fluentd sidecar image tag  |
| fluentd.resources | object | `{}` | Resources requests and limits for fluentd sidecar container See: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/  |
| hostNamespaces | object | `{}` | Share host namespaces which may include hostNetwork, hostIPC, and hostPID  |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy  |
| image.repository | string | `"atlassian/confluence"` | The Confluence Docker image to use https://hub.docker.com/r/atlassian/confluence  |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion  |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress Resource. If using an ingress-nginx controller be sure that the annotations you add here are compatible with those already defined in the 'ingess.yaml' template  |
| ingress.className | string | `"nginx"` | The class name used by the ingress controller if it's being used.  Please follow documentation of your ingress controller. If the cluster contains multiple ingress controllers, this setting allows you to control which of them is used for Atlassian application traffic.  |
| ingress.create | bool | `false` | Set to 'true' if an Ingress Resource should be created. This depends on a pre-provisioned Ingress Controller being available.  |
| ingress.host | string | `nil` | The fully-qualified hostname (FQDN) of the Ingress Resource. Traffic coming in on this hostname will be routed by the Ingress Resource to the appropriate backend Service.  |
| ingress.https | bool | `true` | Set to 'true' if browser communication with the application should be TLS (HTTPS) enforced.  |
| ingress.maxBodySize | string | `"250m"` | The max body size to allow. Requests exceeding this size will result in an HTTP 413 error being returned to the client.  |
| ingress.nginx | bool | `true` | Set to 'true' if the Ingress Resource is to use the K8s 'ingress-nginx' controller. https://kubernetes.github.io/ingress-nginx/  This will populate the Ingress Resource with annotations that are specific to the K8s ingress-nginx controller. Set to 'false' if a different controller is to be used, in which case the appropriate annotations for that controller must be specified below under 'ingress.annotations'.  |
| ingress.openShiftRoute | bool | `false` | Set to true if you want to create an OpenShift Route instead of an Ingress  |
| ingress.path | string | `nil` | The base path for the Ingress Resource. For example '/confluence'. Based on a 'ingress.host' value of 'company.k8s.com' this would result in a URL of 'company.k8s.com/confluence'. Default value is 'confluence.service.contextPath' |
| ingress.proxyConnectTimeout | int | `60` | Defines a timeout for establishing a connection with a proxied server. It should be noted that this timeout cannot usually exceed 75 seconds.  |
| ingress.proxyReadTimeout | int | `60` | Defines a timeout for reading a response from the proxied server. The timeout is set only between two successive read operations, not for the transmission of the whole response. If the proxied server does not transmit anything within this time, the connection is closed.  |
| ingress.proxySendTimeout | int | `60` | Sets a timeout for transmitting a request to the proxied server. The timeout is set only between two successive write operations, not for the transmission of the whole request. If the proxied server does not receive anything within this time, the connection is closed.  |
| ingress.routeHttpHeaders | object | `{}` | routeHttpHeaders defines policy for HTTP headers. Applicable to OpenShift Routes only  |
| ingress.tlsSecretName | string | `nil` | The name of the K8s Secret that contains the TLS private key and corresponding certificate. When utilised, TLS termination occurs at the ingress point where traffic to the Service, and it's Pods is in plaintext.  Usage is optional and depends on your use case. The Ingress Controller itself can also be configured with a TLS secret for all Ingress Resources. https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets https://kubernetes.io/docs/concepts/services-networking/ingress/#tls  |
| monitoring.exposeJmxMetrics | bool | `false` | Expose JMX metrics with jmx_exporter https://github.com/prometheus/jmx_exporter  |
| monitoring.fetchJmxExporterJar | bool | `true` | Fetch jmx_exporter jar from the image. If set to false make sure to manually copy the jar to shared home and provide an absolute path in jmxExporterCustomJarLocation  |
| monitoring.grafana.createDashboards | bool | `false` | Create ConfigMaps with Grafana dashboards  |
| monitoring.grafana.dashboardAnnotations | object | `{}` | Annotations added to Grafana dashboards ConfigMaps. See: https://github.com/kiwigrid/k8s-sidecar#usage  |
| monitoring.grafana.dashboardLabels | object | `{}` | Label selector for Grafana dashboard importer sidecar  |
| monitoring.jmxExporterCustomConfig | object | `{}` | Custom JMX config with the rules  |
| monitoring.jmxExporterCustomJarLocation | string | `nil` | Location of jmx_exporter jar file if mounted from a secret or manually copied to shared home  |
| monitoring.jmxExporterImageRepo | string | `"bitnami/jmx-exporter"` | Image repository with jmx_exporter jar  |
| monitoring.jmxExporterImageTag | string | `"0.18.0"` | Image tag to be used to pull jmxExporterImageRepo  |
| monitoring.jmxExporterInitContainer | object | `{"customSecurityContext":{},"jmxJarLocation":null,"resources":{},"runAsRoot":true}` | JMX exporter init container configuration  |
| monitoring.jmxExporterInitContainer.customSecurityContext | object | `{}` | Custom SecurityContext for the jmx exporter init container  |
| monitoring.jmxExporterInitContainer.jmxJarLocation | string | `nil` | The location of the JMX exporter jarfile in the JMX exporter image Leave blank for default bitnami image  |
| monitoring.jmxExporterInitContainer.resources | object | `{}` | Resources requests and limits for the JMX exporter init container See: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/  |
| monitoring.jmxExporterInitContainer.runAsRoot | bool | `true` | Whether to run JMX exporter init container as root to copy JMX exporter binary to shared home volume. Set to false if running containers as root is not allowed in the cluster.  |
| monitoring.jmxExporterPort | int | `9999` | Port number on which metrics will be available  |
| monitoring.jmxExporterPortType | string | `"ClusterIP"` | JMX exporter port type  |
| monitoring.jmxServiceAnnotations | object | `{}` | Annotations added to the jmx service  |
| monitoring.serviceMonitor.create | bool | `false` | Create ServiceMonitor to start scraping metrics. ServiceMonitor CRD needs to be created in advance.  |
| monitoring.serviceMonitor.prometheusLabelSelector | object | `{}` | ServiceMonitorSelector of the prometheus instance.  |
| monitoring.serviceMonitor.scrapeIntervalSeconds | int | `30` | Scrape interval for the JMX service.  |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Confluence pods  |
| opensearch.credentials.createSecret | bool | `true` | Let the Helm chart create a secret with an auto generated initial admin password  |
| opensearch.credentials.existingSecretRef | object | `{"name":null}` | Use an existing secret with the key OPENSEARCH_INITIAL_ADMIN_PASSWORD holding the initial admin password  |
| opensearch.enabled | bool | `false` | Deploy OpenSearch Helm chart and Configure Confluence to use it as a search platform  |
| opensearch.envFrom[0].secretRef.name | string | `"opensearch-initial-password"` | If using a pre-created secret, make sure to change secret name to match opensearch.credentials.existingSecretRef.name  |
| opensearch.extraEnvs[0].name | string | `"plugins.security.ssl.http.enabled"` |  |
| opensearch.extraEnvs[0].value | string | `"false"` |  |
| opensearch.persistence.size | string | `"10Gi"` |  |
| opensearch.resources.requests.cpu | int | `1` |  |
| opensearch.resources.requests.memory | string | `"1Gi"` |  |
| opensearch.singleNode | bool | `true` | OpenSearch helm specific values, see: https://github.com/opensearch-project/helm-charts/blob/main/charts/opensearch/values.yaml  |
| openshift.runWithRestrictedSCC | bool | `false` | When set to true, the containers will run with a restricted Security Context Constraint (SCC). See: https://docs.openshift.com/container-platform/4.14/authentication/managing-security-context-constraints.html This configuration property unsets pod's SecurityContext, nfs-fixer init container (which runs as root), and mounts server configuration files as ConfigMaps.  |
| ordinals | object | `{"enabled":false,"start":0}` | Set a custom start ordinal number for the K8s stateful set. Note that this depends on the StatefulSetStartOrdinal K8s feature gate, which has entered beta state with K8s version 1.27.  |
| ordinals.enabled | bool | `false` | Enable only if StatefulSetStartOrdinal K8s feature gate is available.  |
| ordinals.start | int | `0` | Set start ordinal to a positive integer, defaulting to 0.  |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Confluence pods  |
| podDisruptionBudget | object | `{"annotations":{},"enabled":false,"labels":{},"maxUnavailable":null,"minAvailable":null}` | PodDisruptionBudget: https://kubernetes.io/docs/tasks/run-application/configure-pdb/ You can specify only one of maxUnavailable and minAvailable in a single PodDisruptionBudget. When both minAvailable and maxUnavailable are set, maxUnavailable takes precedence.  |
| podLabels | object | `{}` | Custom labels that will be applied to all Confluence pods  |
| priorityClassName | string | `nil` | Priority class for the application pods. The PriorityClass with this name needs to be available in the cluster. For details see https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/#priorityclass  |
| replicaCount | int | `1` | The initial number of Confluence pods that should be started at deployment time. Note that Confluence requires manual configuration via the browser post deployment after the first pod is deployed. This configuration must be completed before scaling up additional pods. As such this value should always be kept as 1, but can be altered once manual configuration is complete.  |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Confluence pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods  |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount (if created)  |
| serviceAccount.clusterRole.create | bool | `false` | Set to 'true' if a ClusterRole should be created, or 'false' if it already exists.  |
| serviceAccount.clusterRole.name | string | `nil` | The name of the ClusterRole to be used. If not specified, but the "serviceAccount.clusterRole.create" flag is set to 'true', then the ClusterRole name will be auto-generated.  |
| serviceAccount.clusterRoleBinding.create | bool | `false` | Set to 'true' if a ClusterRoleBinding should be created, or 'false' if it already exists.  |
| serviceAccount.clusterRoleBinding.name | string | `nil` | The name of the ClusterRoleBinding to be created. If not specified, but the "serviceAccount.clusterRoleBinding.create" flag is set to 'true', then the ClusterRoleBinding name will be auto-generated.  |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it already exists.  |
| serviceAccount.eksIrsa.roleArn | string | `nil` |  |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod  |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server  |
| serviceAccount.role.create | bool | `true` | Create a role for Hazelcast client with privileges to get and list pods and endpoints in the namespace. Set to false if you need to create a Role and RoleBinding manually  |
| serviceAccount.roleBinding | object | `{"create":true}` | Grant permissions defined in Role (list and get pods and endpoints) to a service account.  |
| synchrony.additionalAnnotations | string | `nil` | Defines additional annotations to the Synchrony StateFulSet. This might be required when deploying using a GitOps approach |
| synchrony.additionalCertificates | object | `{"customCmd":null,"initContainer":{"resources":{},"securityContext":{}},"secretList":[],"secretName":null}` | Certificates to be added to Java truststore. Provide reference to a secret that contains the certificates  |
| synchrony.additionalCertificates.customCmd | string | `nil` | Custom command to be executed in the init container to import certificates  |
| synchrony.additionalCertificates.initContainer.resources | object | `{}` | Resources allocated to the import-certs init container  |
| synchrony.additionalCertificates.initContainer.securityContext | object | `{}` | Custom SecurityContext for the import-certs init container  |
| synchrony.additionalCertificates.secretList | list | `[]` | A list of secrets with their respective keys holding certificates to be added to the Java truststore. It is mandatory to specify which keys from secret data need to be mounted as files to the init container.  |
| synchrony.additionalCertificates.secretName | string | `nil` | Name of the Kubernetes secret with certificates in its data. All secret keys in the secret data will be treated as certificates to be added to Java truststore. If defined, this takes precedence over secretList.  |
| synchrony.additionalJvmArgs | list | `[]` | Specifies a list of additional arguments that can be passed to the Synchrony JVM, e.g. system properties.  |
| synchrony.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Synchrony container. Each item in the list should specify the name of the volume that contains the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/  |
| synchrony.additionalPorts | list | `[]` | Defines any additional ports for the Synchrony container.  |
| synchrony.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Synchrony container. These can refer to existing volumes, or new volumes can be defined via 'volumes.additionalSynchrony'.  |
| synchrony.affinity | object | `{}` | Standard K8s affinities that will be applied to all Synchrony pods  |
| synchrony.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/  |
| synchrony.enabled | bool | `false` | Set to 'true' if Synchrony (i.e. collaborative editing) should be enabled. This will result in a separate StatefulSet and Service to be created for Synchrony. If disabled, then collaborative editing will be disabled in Confluence. |
| synchrony.hostNamespaces | object | `{}` | Share host namespaces which may include hostNetwork, hostIPC, and hostPID  |
| synchrony.ingress | object | `{"annotations":null,"path":null,"pathType":null}` | If 'synchrony.ingress.path' is defined, a dedicated Synchrony ingress object is created. This is useful if you need to deploy multiple instances of Confluence with Synchrony enabled using the same Ingress hostname and different synchrony paths  |
| synchrony.ingress.annotations | string | `nil` | Custom annotations applied to Synchrony ingress  |
| synchrony.ingress.path | string | `nil` | Ingress path applied to Synchrony ingress  |
| synchrony.ingress.pathType | string | `nil` | Defaults to Prefix, but can be ImplementationSpecific if rewrite target is applied  |
| synchrony.nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Synchrony pods  |
| synchrony.podAnnotations | object | `{}` | Custom annotations that will be applied to all Synchrony pods. When undefined, default to '.Values.podAnnotations' which are Confluence pod annotations (if defined) |
| synchrony.ports.hazelcast | int | `5701` | The port on which the Synchrony container listens for Hazelcast traffic  |
| synchrony.ports.http | int | `8091` | The port on which the Synchrony container listens for HTTP traffic  |
| synchrony.priorityClassName | string | `nil` | Priority class for the Synchrony pods. The PriorityClass with this name needs to be available in the cluster. If not specified the default priorityClassName will be used. For details see https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/#priorityclass  |
| synchrony.readinessProbe.failureThreshold | int | `10` | The number of consecutive failures of the Synchrony container readiness probe before the pod fails readiness checks.  |
| synchrony.readinessProbe.healthcheckPath | string | `"/synchrony/heartbeat"` | The healthcheck path to check against for the Synchrony container useful when configuring behind a reverse-proxy or loadbalancer https://confluence.atlassian.com/confkb/cannot-enable-collaborative-editing-on-synchrony-cluster-962962742.html  |
| synchrony.readinessProbe.initialDelaySeconds | int | `5` | The initial delay (in seconds) for the Synchrony container readiness probe, after which the probe will start running.  |
| synchrony.readinessProbe.periodSeconds | int | `1` | How often (in seconds) the Synchrony container readiness probe will run  |
| synchrony.replicaCount | int | `1` | Number of Synchrony pods  |
| synchrony.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Synchrony pod. Because the container CPU request value is used in -XX:ActiveProcessorCount argument to Synchrony JVM  only integers are allowed, e.g. 1, 2, 3 etc. If you want to have a small CPU claim, set it to 30m, 50m, etc. Any container cpu request value containing `m` character will be converted to -XX:ActiveProcessorCount=1  See: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#meaning-of-cpu  |
| synchrony.resources.container.requests.memory | string | `"2.5G"` | Initial Memory request Synchrony pod  |
| synchrony.resources.jvm.maxHeap | string | `"2g"` | The minimum amount of heap memory that will be used by the Synchrony JVM  |
| synchrony.resources.jvm.minHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Synchrony JVM  |
| synchrony.resources.jvm.stackSize | string | `"2048k"` | The memory allocated for the Synchrony stack  |
| synchrony.schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Synchrony pods. If not specified, the default schedulerName will be used. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods  |
| synchrony.securityContext.fsGroup | int | `2002` | The GID used by the Confluence docker image GID will default to 2002 if not supplied and securityContextEnabled is set to true. This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Confluence container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260 |
| synchrony.securityContextEnabled | bool | `true` |  |
| synchrony.service.annotations | object | `{}` | Annotations to apply to Synchrony Service  |
| synchrony.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer.  |
| synchrony.service.nodePort | string | `nil` | Only applicable if service.type is NodePort. NodePort for Synchrony service  |
| synchrony.service.port | int | `80` | The port on which the Synchrony K8s Service will listen  |
| synchrony.service.type | string | `"ClusterIP"` | The type of K8s service to use for Synchrony  |
| synchrony.setPermissions | bool | `true` | Boolean to define whether to set synchrony home directory permissions on startup of Synchrony container. Set to 'false' to disable this behaviour.  |
| synchrony.shutdown.terminationGracePeriodSeconds | int | `25` | The termination grace period for pods during shutdown. This should be set to the Synchrony internal grace period (default 20 seconds), plus a small buffer to allow the JVM to fully terminate. |
| synchrony.tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Synchrony pods  |
| synchrony.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Synchrony pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/  |
| testPods | object | `{"affinity":{},"annotations":{},"image":{"permissionsTestContainer":"debian:stable-slim","statusTestContainer":"alpine:latest"},"labels":{},"nodeSelector":{},"resources":{},"schedulerName":null,"tolerations":[]}` | Metadata and pod spec for pods started in Helm tests  |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Confluence pods  |
| updateStrategy | object | `{}` | StatefulSet update strategy. When unset defaults to Rolling update. See: https://kubernetes.io/docs/tutorials/stateful-application/basic-stateful-set/#updating-statefulsets  |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Confluence pods. Note that this will not create any corresponding volume mounts; those needs to be defined in confluence.additionalVolumeMounts  |
| volumes.additionalSynchrony | list | `[]` | Defines additional volumes that should be applied to all Synchrony pods. Note that this will not create any corresponding volume mounts; those needs to be defined in synchrony.additionalVolumeMounts  |
| volumes.defaultPermissionsMode | int | `484` | Mode bits used to set permissions on created files by default. Must be an octal value between 0000 and 0777 or a decimal value between 0 and 511 Typically overridden in volumes from Secrets and ConfigMaps to make mounted files executable  |
| volumes.localHome.customVolume | object | `{}` | Static provisioning of local-home using K8s PVs and PVCs  NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for pods is not recommended. Dynamic provisioning described above is the prescribed approach.  When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the local-home volume(s). If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static  |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/confluence"` | Specifies the path in the Confluence container to which the local-home volume will be mounted.  |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically created for each pod based on the 'StorageClassName' supplied below.  |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests for the local-home volume claims.  |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the local-home volume claim.  |
| volumes.localHome.persistentVolumeClaimRetentionPolicy.whenDeleted | string | `nil` | Configures the volume retention behavior that applies when the StatefulSet is deleted.  |
| volumes.localHome.persistentVolumeClaimRetentionPolicy.whenScaled | string | `nil` | Configures the volume retention behavior that applies when the replica count of the StatefulSet is reduced.  |
| volumes.localHome.subPath | string | `nil` | Specifies the sub-directory of the local-home volume that will be mounted in to the Confluence container.  |
| volumes.sharedHome.customVolume | object | `{}` | Static provisioning of shared-home using K8s PVs and PVCs  When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the shared-home volume. If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static https://atlassian.github.io/data-center-helm-charts/examples/storage/aws/SHARED_STORAGE/  |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Confluence container to which the shared-home volume will be mounted.  |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Confluence container's GID (2002), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here.  |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `true` | If 'true', this will alter the shared-home volume's root directory so that Confluence can write to it. This is a workaround for a K8s bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260  |
| volumes.sharedHome.nfsPermissionFixer.imageRepo | string | `"alpine"` | Image repository for the permission fixer init container. Defaults to alpine  |
| volumes.sharedHome.nfsPermissionFixer.imageTag | string | `"latest"` | Image tag for the permission fixer init container. Defaults to latest  |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the K8s initContainer where the shared-home volume will be mounted  |
| volumes.sharedHome.nfsPermissionFixer.resources | object | `{}` | Resources requests and limits for nfsPermissionFixer init container See: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/  |
| volumes.sharedHome.persistentVolumeClaim.accessModes | list | `["ReadWriteMany"]` | Specify the access modes that should be used for the 'shared-home' volume claim. Note: 'ReadWriteOnce' (RWO) is suitable only for single-node installations. Be aware that changing the access mode of an existing PVC might be impossible, as the PVC spec is immutable. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#access-modes  |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolumeClaim' and 'PersistentVolume' will be dynamically created for shared-home based on the 'StorageClassName' supplied below.  |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests limits for the shared-home volume claims.  |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the 'shared-home' volume claim.  |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume that will be mounted in to the Confluence container.  |
| volumes.synchronyHome.customVolume | object | `{}` | Static provisioning of synchrony-home using K8s PVs and PVCs  NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for pods is not recommended. Dynamic provisioning described above is the prescribed approach.  When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the synchrony-home volume(s). If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static  |
| volumes.synchronyHome.mountPath | string | `"/var/atlassian/application-data/confluence"` | Specifies the path in the Synchrony container to which the synchrony-home volume will be mounted.  |
| volumes.synchronyHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically created for each pod based on the 'StorageClassName' supplied below.  |
| volumes.synchronyHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests for the synchrony-home volume claims.  |
| volumes.synchronyHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the synchrony-home volume claim.  |
| volumes.synchronyHome.persistentVolumeClaimRetentionPolicy.whenDeleted | string | `nil` | Configures the volume retention behavior that applies when the StatefulSet is deleted.  |
| volumes.synchronyHome.persistentVolumeClaimRetentionPolicy.whenScaled | string | `nil` | Configures the volume retention behavior that applies when the replica count of the StatefulSet is reduced.  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.12.0](https://github.com/norwoodj/helm-docs/releases/v1.12.0)
