# Change Log


## 0.14.0

![AppVersion: 4.3.5-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.5-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-529: Update Crowd version to 4.3.5-jdk11 (#212)


## 0.13.0

![AppVersion: 4.3.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.0-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-54: Volume docs updates (#188)


## 0.12.0 

![AppVersion: 4.3.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.0-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-437: Wording improvement for warning in NOTES when PV is not used (#199) 
* DCKUBE-393: Improve readability of Crowd values.yaml file (#184) 
* Defining the following values in the helpers template for each chart, to allow template overrides: (#173) 

### Default value changes

There has been major improvement in the documentation for the keys in `values.yaml` file but there isn't any functional
change.

## 0.11.0 

**Release date:** 2021-06-09

![AppVersion: 4.3.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.0-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-348: Warning of absent persistent volume (#169) 
* DCKUBE-307: Do not print logs when testing helm installation. (#168) 
* DCKUBE-308: Print service URL after installing helm chart (#157) 
* DCKUBE-282: Update icons to SVG (#164) 

### Default value changes

```diff
# No changes in this release
```

## 0.10.0 

**Release date:** 2021-06-01

![AppVersion: 4.3.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.0-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Version 0.10.0 
* DCKUBE-332: Update the minimal supported kubernetes version v1.19 (#154) 

### Default value changes

```diff
# No changes in this release
```

## 0.9.0 

**Release date:** 2021-05-25

![AppVersion: 4.3.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.0-jdk11&color=success&logo=)
![Kubernetes: >=1.17.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.17.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Version 0.9.0 
* initial commit - changed the max-body-size of request to 250MB for all products and documented in CONFIG.md (#140) 
* Merge branch 'master' into dckube-267-define-minimum-compute-resources 
* README update for 0.8.0 
* Merge branch 'dckube-267-define-minimum-compute-resources' of github.com:atlassian-labs/data-center-helm-charts into dckube-267-define-minimum-compute-resources 
* DCKUBE-267: Update cpu request sizes 
* Merge branch 'master' into dckube-267-define-minimum-compute-resources 
* DCKUBE-267: Update cpu request sizes 
* Added ingress.host into values.yaml with corresponding value injection in ingress.yaml for all apps. Defaults to / (#134) 
* DCKUBE-267: Wording updates 
* DCKUBE-267: Initial commit 
* Crowd 0.7.0 README update 

### Default value changes

```diff
diff --git a/src/main/charts/crowd/values.yaml b/src/main/charts/crowd/values.yaml
index a48f866..ce5a29d 100644
--- a/src/main/charts/crowd/values.yaml
+++ b/src/main/charts/crowd/values.yaml
@@ -76,28 +76,30 @@ crowd:
 
   resources:
     jvm:
+      # -- JVM memory arguments below are based on the defaults defined for the Crowd docker container, see:
+      # https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/src/master/
+      #
       # -- The maximum amount of heap memory that will be used by the Crowd JVM
-      maxHeap: "1g"
+      maxHeap: "768m"
       # -- The minimum amount of heap memory that will be used by the Crowd JVM
-      minHeap: "1g"
-      # -- The memory reserved for the Crowd JVM code cache
-      reservedCodeCache: "512m"
+      minHeap: "384m"
     # -- Specifies the standard Kubernetes resource requests and/or limits for the Crowd container.
     # It is important that if the memory resources are specified here, they must allow for the size of the Crowd JVM.
     # That means the maximum heap size, the reserved code cache size, plus other JVM overheads, must be accommodated.
-    # Allowing for (maxHeap+codeCache)*1.5 would be an example.
-    container: {}
+    # Allowing for maxHeap * 1.5 would be an example.
+    container: 
     #  limits:
-    #    cpu: "4"
-    #    memory: "2G"
-    #  requests:
-    #    cpu: "4"
-    #    memory: "2G"
+    #    cpu: "1"
+    #    memory: "1G"
+      requests:
+        cpu: "2" # -- If changing the cpu value update additional JVM arg 'ActiveProcessorCount' below
+        memory: "1G"
 
   # -- Specifies a list of additional arguments that can be passed to the Crowd JVM, e.g. system properties
-  additionalJvmArgs: []
-#    - -Dfoo=bar
-#    - -Dfruit=lemon
+  additionalJvmArgs: 
+    # -- The value defined for ActiveProcessorCount should correspond to that provided for 'container.requests.cpu'
+    # see: https://docs.oracle.com/en/java/javase/11/tools/java.html#GUID-3B1CE181-CD30-4178-9602-230B800D4FAE
+    - -XX:ActiveProcessorCount=2
 
   # -- Specifies a list of additional Java libraries that should be added to the Crowd container.
   # Each item in the list should specify the name of the volume which contain the library, as well as the name of the
@@ -136,9 +138,11 @@ ingress:
   # -- The max body size to allow. Requests exceeding this size will result
   # in an 413 error being returned to the client.
   # https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#custom-max-body-size
-  maxBodySize: 10m
+  maxBodySize: 250m
   # -- The fully-qualified hostname of the Ingress Resource.
   host:
+  # -- The base path for the ingress rule.
+  path: "/"
   # -- The custom annotations that should be applied to the Ingress
   # Resource when not using the Kubernetes ingress-nginx controller.
   annotations: {}
```

## 0.7.0 

**Release date:** 2021-05-13

![AppVersion: 4.3.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.0-jdk11&color=success&logo=)
![Kubernetes: >=1.17.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.17.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Add Crowd as a tested product (#128) 

### Default value changes

```diff
# No changes in this release
```

## 0.1.0 

**Release date:** 2021-05-13

![AppVersion: 4.3.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=4.3.0-jdk11&color=success&logo=)
![Kubernetes: >=1.17.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.17.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* initial Crowd Helm Chart based on Confluence Helm Chart (#106) 

### Default value changes

```diff
# -- The initial number of pods that should be started at deployment of each of Crowd.
# Note that because Crowd requires initial manual configuration after the first pod is deployed, and before scaling
# up to additional pods, this should always be kept as 1.
replicaCount: 1

image:
  repository: atlassian/crowd
  pullPolicy: IfNotPresent
  # -- The docker image tag to be used. Defaults to the Chart appVersion.
  tag: 4.2.2

serviceAccount:
  # -- Specifies the name of the ServiceAccount to be used by the pods.
  # If not specified, but the the "serviceAccount.create" flag is set, then the ServiceAccount name will be auto-generated,
  # otherwise the 'default' ServiceAccount will be used.
  name:
  # -- true if a ServiceAccount should be created, or false if it already exists
  create: true
  # -- The list of image pull secrets that should be added to the created ServiceAccount
  imagePullSecrets: []
  clusterRole:
    # -- Specifies the name of the ClusterRole that will be created if the "serviceAccount.clusterRole.create" flag is set.
    # If not specified, a name will be auto-generated.
    name:
    # -- true if a ClusterRole should be created, or false if it already exists
    create: true
  clusterRoleBinding:
    # -- Specifies the name of the ClusterRoleBinding that will be created if the "serviceAccount.clusterRoleBinding.create" flag is set
    # If not specified, a name will be auto-generated.
    name:
    # -- true if a ClusterRoleBinding should be created, or false if it already exists
    create: true

crowd:
  service:
    # -- The port on which the Crowd Kubernetes service will listen
    port: 80
    # -- The type of Kubernetes service to use for Crowd
    type: ClusterIP
  # -- Enable or disable security context in StatefulSet template spec. Enabled by default with UID 2002.
  # -- Disable when deploying to OpenShift, unless anyuid policy is attached to a service account
  securityContext:
    enabled: true
    # -- The GID used by the Crowd docker image
    gid: "2004"
  # -- The umask used by the Crowd process when it creates new files.
  # Default is 0022, which makes the new files read/writeable by the Crowd user, and readable by everyone else.
  umask: "0022"
  ports:
    # -- The port on which the Crowd container listens for HTTP traffic
    http: 8095
    # -- The port on which the Crowd container listens for Hazelcast traffic
    hazelcast: 5701
  readinessProbe:
    # -- The initial delay (in seconds) for the Crowd container readiness probe, after which the probe will start running
    initialDelaySeconds: 10
    # -- How often (in seconds) the Crowd container readiness robe will run
    periodSeconds: 5
    # -- The number of consecutive failures of the Crowd container readiness probe before the pod fails readiness checks
    failureThreshold: 30

  accessLog:
    # -- True if access logging should be enabled.
    enabled: true
    # -- The path within the Crowd container where the local-home volume should be mounted in order to capture access logs.
    mountPath: "/opt/atlassian/crowd/logs"
    # -- The subdirectory within the local-home volume where access logs should be stored.
    localHomeSubPath: "logs"

  clustering:
    # -- Set to true if Data Center clustering should be enabled
    # This will automatically configure cluster peer discovery between cluster nodes.
    enabled: false
    # -- Set to true if the Kubernetes pod name should be used as the end-user-visible name of the Data Center cluster node.
    usePodNameAsClusterNodeName: true

  resources:
    jvm:
      # -- The maximum amount of heap memory that will be used by the Crowd JVM
      maxHeap: "1g"
      # -- The minimum amount of heap memory that will be used by the Crowd JVM
      minHeap: "1g"
      # -- The memory reserved for the Crowd JVM code cache
      reservedCodeCache: "512m"
    # -- Specifies the standard Kubernetes resource requests and/or limits for the Crowd container.
    # It is important that if the memory resources are specified here, they must allow for the size of the Crowd JVM.
    # That means the maximum heap size, the reserved code cache size, plus other JVM overheads, must be accommodated.
    # Allowing for (maxHeap+codeCache)*1.5 would be an example.
    container: {}
    #  limits:
    #    cpu: "4"
    #    memory: "2G"
    #  requests:
    #    cpu: "4"
    #    memory: "2G"

  # -- Specifies a list of additional arguments that can be passed to the Crowd JVM, e.g. system properties
  additionalJvmArgs: []
#    - -Dfoo=bar
#    - -Dfruit=lemon

  # -- Specifies a list of additional Java libraries that should be added to the Crowd container.
  # Each item in the list should specify the name of the volume which contain the library, as well as the name of the
  # library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which
  # directory in the volume contains the library file.
  additionalLibraries: []
#    - volumeName:
#      subDirectory:
#      fileName:

  # -- Specifies a list of additional Crowd plugins that should be added to the Crowd container.
  # These are specified in the same manner as the additionalLibraries field, but the files will be loaded
  # as bundled plugins rather than as libraries.
  additionalBundledPlugins: []
#    - volumeName:
#      subDirectory:
#      fileName:

  # -- Defines any additional volumes mounts for the Crowd container.
  # These can refer to existing volumes, or new volumes can be defined in volumes.additional.
  additionalVolumeMounts: []

  # -- Defines any additional environment variables to be passed to the Crowd container.
  # See https://hub.docker.com/r/atlassian/crowd for supported variables.
  additionalEnvironmentVariables: []

ingress:
  # -- True if an Ingress Resource should be created.
  create: false
  # -- True if the created Ingress Resource is to use the Kubernetes ingress-nginx controller:
  # https://kubernetes.github.io/ingress-nginx/
  # This will populate the Ingress Resource with annotations for the Kubernetes ingress-nginx controller.
  # Set to false if a different controller is to be used, in which case the appropriate annotations for that
  # controller need to be specified.
  nginx: true
  # -- The max body size to allow. Requests exceeding this size will result
  # in an 413 error being returned to the client.
  # https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#custom-max-body-size
  maxBodySize: 10m
  # -- The fully-qualified hostname of the Ingress Resource.
  host:
  # -- The custom annotations that should be applied to the Ingress
  # Resource when not using the Kubernetes ingress-nginx controller.
  annotations: {}
  # -- True if the browser communicates with the application over HTTPS.
  https: true
  # -- Secret that contains a TLS private key and certificate.
  # Optional if Ingress Controller is configured to use one secret for all ingresses
  tlsSecretName:

fluentd:
  # -- True if the fluentd sidecar should be added to each pod
  enabled: false
  # -- The name of the image containing the fluentd sidecar
  imageName: fluent/fluentd-kubernetes-daemonset:v1.11.5-debian-elasticsearch7-1.2
  # -- The port on which the fluentd sidecar will listen
  httpPort: 9880
  elasticsearch:
    # -- True if fluentd should send all log events to an elasticsearch service.
    enabled: true
    # -- The hostname of the Elasticsearch service that fluentd should send logs to.
    hostname: elasticsearch
    # -- The prefix of the elasticsearch index name that will be used
    indexNamePrefix: crowd

# -- Specify additional annotations to be added to all Crowd pods
podAnnotations: {}
#  "name": "value"

volumes:
  localHome:
    persistentVolumeClaim:
      # -- If true, then a PersistentVolumeClaim will be created for each local-home volume.
      create: false
      # -- Specifies the name of the storage class that should be used for the local-home volume claim.
      storageClassName:
      # -- Specifies the standard Kubernetes resource requests and/or limits for the local-home volume claims.
      resources:
        requests:
          storage: 1Gi
    # -- When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes
    # volume which will be used for the local-home volumes. If not defined, then defaults to an emptyDir volume.
    customVolume: {}
    # -- The path within the Crowd container which the local-home volume should be mounted.
    mountPath: "/var/atlassian/application-data/crowd"
  sharedHome:
    persistentVolumeClaim:
      # -- If true, then a PersistentVolumeClaim will be created for the shared-home volume.
      create: false
      # -- Specifies the name of the storage class that should be used for the shared-home volume claim.
      storageClassName:
      # -- Specifies the standard Kubernetes resource requests and/or limits for the shared-home volume claims.
      resources:
        requests:
          storage: 1Gi
    # -- When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes
    # volume which will be used for the shared-home volume. If not defined, then defaults to an emptyDir (i.e. unshared) volume.
    customVolume: {}
    # -- Specifies the path in the Crowd container to which the shared-home volume will be mounted.
    mountPath: "/var/atlassian/application-data/crowd/shared"
    # -- Specifies the sub-directory of the shared-home volume which will be mounted in to the Crowd container.
    subPath:
    nfsPermissionFixer:
      # -- If enabled, this will alter the shared volume's root directory so that Crowd can write to it.
      # This is a workaround for a Kubernetes bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260
      enabled: false
      # -- The path in the initContainer where the shared-home volume will be mounted
      mountPath: /shared-home
      # -- By default, the fixer will change the group ownership of the volume's root directory to match the Crowd
      # container's GID (2002), and then ensures the directory is group-writeable. If this is not the desired behaviour,
      # command used can be specified here.
      command:
  # -- Defines additional volumes that should be applied to all Crowd pods.
  # Note that this will not create any corresponding volume mounts;
  # those needs to be defined in crowd.additionalVolumeMounts
  additional: []

# -- Standard Kubernetes node-selectors that will be applied to all Crowd pods
nodeSelector: {}

# -- Standard Kubernetes tolerations that will be applied to all Crowd pods
tolerations: []

# -- Standard Kubernetes affinities that will be applied to all Crowd pods
affinity: {}

# -- Additional container definitions that will be added to all Crowd pods
additionalContainers: []

# -- Additional initContainer definitions that will be added to all Crowd pods
additionalInitContainers: []

# -- Additional labels that should be applied to all resources
additionalLabels: {}

# -- Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into server container
# configMap and secret are two available types (camelCase is important!)
# mountPath is a destination directory in a container and key is file name
# name references existing ConfigMap or secret name. VolumeMount and Volumes are added with this name + index position,
# for example custom-config-0, keystore-2
additionalFiles: []
#  - name: custom-config
#    type: configMap
#    key: log4j.properties
#    mountPath:  /var/atlassian
#  - name: custom-config
#    type: configMap
#    key: web.xml
#    mountPath: /var/atlassian
#  - name: keystore
#    type: secret
#    key: keystore.jks
#    mountPath: /var/ssl
```

---
Autogenerated from Helm Chart and git history using [helm-changelog](https://github.com/mogensen/helm-changelog)
