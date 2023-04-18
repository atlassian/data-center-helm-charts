# Change Log

## 1.12.0

**Release date:** 2023-4-18

![AppVersion: 8.9.0](https://img.shields.io/static/v1?label=AppVersion&message=8.9.0&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add priorityClassName to pod spec (#557)
* Add hostAliases for all DC products (#556)
* Update appVersions for DC apps (#558)

## 1.11.0

**Release date:** 2023-3-22

![AppVersion: 7.21.10](https://img.shields.io/static/v1?label=AppVersion&message=7.21.10&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update appVersions for DC apps (#531)

## 1.10.0

**Release date:** 2023-2-20

![AppVersion: 7.21.10](https://img.shields.io/static/v1?label=AppVersion&message=7.21.10&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add Bitbucket Mesh to Bitbucket Helm chart (#501)

## 1.9.1

**Release date:** 2023-2-16

![AppVersion: 7.21.10](https://img.shields.io/static/v1?label=AppVersion&message=7.21.10&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Fix artifact hub annotation yaml

## 1.9.0

**Release date:** 2023-2-15

![AppVersion: 7.21.10](https://img.shields.io/static/v1?label=AppVersion&message=7.21.10&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update appVersion to the latest LTS (#515)

## 1.8.1

**Release date:** 2022-12-12

![AppVersion: 7.21.7](https://img.shields.io/static/v1?label=AppVersion&message=7.21.7&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Fix artifacthub.io annotations

## 1.8.0

**Release date:** 2022-12-9

![AppVersion: 7.21.7](https://img.shields.io/static/v1?label=AppVersion&message=7.21.7&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* CLIP-1644: Stop supporting 1.19-1.20 k8s (#486)
* CLIP-1702: Create Role instead of ClusterRole for Hazelcast Kube client in Confluence and Bitbucket Helm charts (#470)
* Update appVersion to 7.21.7
* Use `ingress.https` flag to enable tls in ingress (#487)
* Replace deprecated `ELASTICSEARCH_ENABLED` with `SEARCH_ENABLED` env var (#488)

## 1.7.1

**Release date:** 2022-10-26

![AppVersion: 9.0.0](https://img.shields.io/static/v1?label=AppVersion&message=9.0.0&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Patch release to fix Artifacthub metadata

## 1.7.0

**Release date:** 2022-10-25

![AppVersion: 7.21.5](https://img.shields.io/static/v1?label=AppVersion&message=7.21.5&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Pin Python version to 3.9.14 (#468)
* Update Bitbucket ingress testing values (#466)
* Ingress Class Name is moved under spec field. (#451)
* Make ATL_FORCE_CFG_UPDATE configurable in values.yaml (#454)
* Add osquery related env vars for Terraform (#462)
* AWS cleanup and log colletion from k8s (#461)

## 1.6.0

**Release date:** 2022-10-12

![AppVersion: 7.21.5](https://img.shields.io/static/v1?label=AppVersion&message=7.21.5&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update Bitbucket version to 7.21.5 (#455)
* Improved documentation (#448, #440)


## 1.5.0

**Release date:** 2022-07-14

![AppVersion: 7.21.2](https://img.shields.io/static/v1?label=AppVersion&message=7.21.2&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Fix: Use the custom ports for Bitbucket service (#419)
* Update Bitbucket version to 7.21.2 (#430)

## 1.4.0

**Release date:** 2022-05-25

![AppVersion: 7.21.1](https://img.shields.io/static/v1?label=AppVersion&message=7.21.1&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Remove emptyDir from Bitbucket shared home volume options (#386)
* Make pod securityContext optional (#389)
* Support for configuring ingress proxy settings via values.yaml (#402)
* Update Bitbucket version to 7.21.1 (#412)

## 1.3.0

**Release date:** 2022-03-24

![AppVersion: 7.21.0](https://img.shields.io/static/v1?label=AppVersion&message=7.21.0&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Update Bitbucket version to 7.21.0 (#396)

## 1.2.0

**Release date:** 2022-02-14

![AppVersion: 7.17.5-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.17.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Use volumeName only if explicitly defined (#372)
* Fixed templating of PLUGIN_SSH_BASEURL with custom port (#371)
* DCD-1452: Updated appVersion to the latest product LTS version. (#378)
* Improvements on [documentation](https://github.com/atlassian/data-center-helm-charts/) (#370, #357)
* Updated Atlassian charts to use common definitions (#303)
* Added service account annotation (#363)
* Added new feature additionalVolumeClaimTemplates and provided example in documentation (#334, #368)
* Added new feature podLabels (#364)
* Added new feature to define loadBalancerIP (#365)
* Define podAnnotations as template to allow overrides (#341)
* DCKUBE-738: Added topologySpreadConstraints to products (#351)
* Set ActiveProcessorCount automatically based on Values.<product>.resources.container.requests.cpu (#352)
* Added new feature additionalPorts (for jmx-monitoring) (#353)


## 1.1.0

**Release date:** 2021-11-03

![AppVersion: 7.17.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.17.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-721: Update version in Chart.yaml files
* DCKUBE-733: Update the product versions (#345)
* DCKUBE-731: Stabilize CI (#344)
* DCKUBE-731: Fix shared home default (#342)
* BAMBK8S-117: Documentation updates for Bamboo on K8s (#336)
* DCKUBE-739: Fix typos (#337)
* DCKUBE-739: Make securityContext changes backward compatible (#332)
* DCKUBE-552 Mirror support. (#265)
* Roll Statefulset Pods if ConfigMap changes (#315)
* DCKUBE-677: Make security context more flexible (#321)
* DCKUBE-634: Bitbucket - set context path (#314)
* DCKUBE-722: Enable configuring ingress.class name (#313)
* DCKUBE-678: Add schedulerName to StatefulSet (#301)


## 1.0.0

This is the first officially supported version of the Helm chart.

![AppVersion: 7.15.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.15.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-621: Improvements to graceful shutdown (#282)
* Improved [documentation](https://github.com/atlassian/data-center-helm-charts/) (#275, #276, #277, #279, #280, #284, #285, #289, #290, #291, #293. #295)


## 0.16.0

![AppVersion: 7.15.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.15.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-598: Enable NFS permission fixer by default (#241)
* DCKUBE-581: Enable configuration for SET_PERMISSIONS docker image variable (#261)
* DCKUBE-613: Configurable grace periods (#249)
* Update the Bitbucket image name, as the '-server' suffix is now deprecated (#259)
* Improve [documentation](https://github.com/atlassian/data-center-helm-charts/) (#236, #243, #245, #252, #253, #256, #258, #260, #268, #270, #272)


## 0.15.0

![AppVersion: 7.15.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.15.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)



* DCKUBE-435: Renamed the 'master' branch to 'main' (#232)
* DCKUBE-453: Add support for providing a custom fluentd start command (#218)
* DCKUBE-534: Make some deployment params configurable (#226)
* DCKUBE-596: Update Bitbucket version to 7.15.1-jdk11 (#238)
* Update EKS cluster yaml example (#227)
* Improve [documentation](https://github.com/atlassian/data-center-helm-charts/) (#206, #222, #223, #228, #229, #231, #233, #235)


## 0.14.0

![AppVersion: 7.14.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.14.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-529: Update Bitbucket version to 7.14.1-jdk11 (#212)


## 0.13.0

![AppVersion: 7.12.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-436: Bitbucket NFS server chart. (#205)
* DCKUBE-54: Volume docs updates (#188)


## 0.12.0

![AppVersion: 7.12.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-437: Wording improvement for warning in NOTES when PV is not used (#199)
* DCKUBE-410: fix additional injections (#186)
* DCKUBE-390: Improve readability of Jira values.yaml file (#179)
* DCKUBE-391: Improve readability of Bitbucket values.yaml file (#182)
* Defining the following values in the helpers template for each chart, to allow template overrides: (#173)

### Default value changes

There has been major improvement in the documentation for the keys in `values.yaml` file but there isn't any functional
change.

## 0.11.0

**Release date:** 2021-06-09

![AppVersion: 7.12.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-348: Warning of absent persistent volume (#169)
* DCKUBE-307: Do not print logs when testing helm installation. (#168)
* DCKUBE-308: Print service URL after installing helm chart (#157)
* DCKUBE-282: Update icons to SVG (#164)
* DCKUBE-302: Add Bitbucket SSH service and custom annotations. (#152)

### Default value changes

```diff
diff --git a/src/main/charts/bitbucket/values.yaml b/src/main/charts/bitbucket/values.yaml
index d3342d2..786d343 100644
--- a/src/main/charts/bitbucket/values.yaml
+++ b/src/main/charts/bitbucket/values.yaml
@@ -52,6 +52,17 @@ bitbucket:
     port: 80
     # -- The type of Kubernetes service to use for Bitbucket
     type: ClusterIP
+    annotations: {}
+  # -- Enable or disable an additional service for exposing SSH for external access.
+  # Disable when the SSH service is exposed through the ingress controller, or enable if the ingress controller does
+  # not support TCP.
+  sshService:
+    enabled: false
+    # -- Port to expose the SSH service on.
+    port: 22
+    type: LoadBalancer
+    # -- Annotations for the SSH service. Useful if a load balancer controller needs extra annotations.
+    annotations: {}
   # -- Enable or disable security context in StatefulSet template spec. Enabled by default with UID 2003.
   # -- Disable when deploying to OpenShift, unless anyuid policy is attached to service account
   securityContext:
```

## 0.10.0

**Release date:** 2021-06-01

![AppVersion: 7.12.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.1-jdk11&color=success&logo=)
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

![AppVersion: 7.12.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.1-jdk11&color=success&logo=)
![Kubernetes: >=1.17.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.17.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Version 0.9.0
* initial commit - changed the max-body-size of request to 250MB for all products and documented in CONFIG.md (#140)
* Update values.yaml (#139)
* Merge branch 'master' into dckube-267-define-minimum-compute-resources
* README update for 0.8.0
* Merge branch 'dckube-267-define-minimum-compute-resources' of github.com:https://github.com/atlassian/data-center-helm-charts/data-center-helm-charts into dckube-267-define-minimum-compute-resources
* DCKUBE-267: Update cpu request sizes
* Merge branch 'master' into dckube-267-define-minimum-compute-resources
* Merge branch 'dckube-267-define-minimum-compute-resources' of github.com:https://github.com/atlassian/data-center-helm-charts/data-center-helm-charts into dckube-267-define-minimum-compute-resources
* DCKUBE-267: Update cpu request sizes
* Added ingress.host into values.yaml with corresponding value injection in ingress.yaml for all apps. Defaults to / (#134)
* Merge branch 'master' into dckube-267-define-minimum-compute-resources
* DCKUBE-267: Wording updates
* DCKUBE-267: Initial commit
* DCKUBE-51 Application upgrade (#122)
* Add Crowd as a tested product (#128)

### Default value changes

```diff
diff --git a/src/main/charts/bitbucket/values.yaml b/src/main/charts/bitbucket/values.yaml
index bb7a568..d3342d2 100644
--- a/src/main/charts/bitbucket/values.yaml
+++ b/src/main/charts/bitbucket/values.yaml
@@ -9,7 +9,7 @@ image:

 serviceAccount:
   # -- Specifies the name of the ServiceAccount to be used by the pods.
-  # If not specified, but the the "serviceAccount.create" flag is set, then the ServiceAccount name will be auto-generated,
+  # If not specified, but the "serviceAccount.create" flag is set, then the ServiceAccount name will be auto-generated,
   # otherwise the 'default' ServiceAccount will be used.
   name:
   # -- true if a ServiceAccount should be created, or false if it already exists
@@ -31,10 +31,10 @@ serviceAccount:

 database:
   # -- The JDBC URL of the database to be used by Bitbucket, e.g. jdbc:postgresql://host:port/database
-  # If not specified, then it will need to be provided via browser during initial startup.
+  # If not specified, then it will need to be provided via the browser during initial startup.
   url:
   # -- The Java class name of the JDBC driver to be used, e.g. org.postgresql.Driver
-  # If not specified, then it will need to be provided via browser during initial startup.
+  # If not specified, then it will need to be provided via the browser during initial startup.
   driver:
   credentials:
     # -- The name of the Kubernetes Secret that contains the database login credentials.
@@ -64,25 +64,25 @@ bitbucket:
     hazelcast: 5701

   license:
-    # -- The name of the Kubernetes Secret which contains the Bitbucket license key.
+    # -- The name of the Kubernetes Secret that contains the Bitbucket license key.
     # If specified, then the license will be automatically populated during Bitbucket setup.
     # Otherwise, it will need to be provided via the browser after initial startup.
     secretName:
-    # -- The key in the Kubernetes Secret which contains the Bitbucket license key
+    # -- The key in the Kubernetes Secret that contains the Bitbucket license key
     secretKey: license-key

   sysadminCredentials:
-    # -- The name of the Kubernetes Secret which contains the Bitbucket sysadmin credentials
+    # -- The name of the Kubernetes Secret that contains the Bitbucket sysadmin credentials
     # If specified, then these will be automatically populated during Bitbucket setup.
     # Otherwise, they will need to be provided via the browser after initial startup.
     secretName:
-    # -- The key in the Kubernetes Secret which contains the sysadmin username
+    # -- The key in the Kubernetes Secret that contains the sysadmin username
     usernameSecretKey: username
-    # -- The key in the Kubernetes Secret which contains the sysadmin password
+    # -- The key in the Kubernetes Secret that contains the sysadmin password
     passwordSecretKey: password
-    # -- The key in the Kubernetes Secret which contains the sysadmin display name
+    # -- The key in the Kubernetes Secret that contains the sysadmin display name
     displayNameSecretKey: displayName
-    # -- The key in the Kubernetes Secret which contains the sysadmin email address
+    # -- The key in the Kubernetes Secret that contains the sysadmin email address
     emailAddressSecretKey: emailAddress

   clustering:
@@ -104,30 +104,34 @@ bitbucket:

   resources:
     jvm:
-      # -- The maximum amount of heap memory that will be used by the Bitbucket JVM. The same value will be used by the ElasticSearch JVM.
+      # -- JVM memory arguments below are based on the defaults defined for the Bitbucket docker container, see:
+      # https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-server/src/master/
+      #
+      # -- The maximum amount of heap memory that will be used by the Bitbucket JVM. The same value will be used by the Elasticsearch JVM.
       maxHeap: "1g"
-      # -- The minimum amount of heap memory that will be used by the Bitbucket JVM. The same value will be used by the ElasticSearch JVM.
-      minHeap: "1g"
+      # -- The minimum amount of heap memory that will be used by the Bitbucket JVM. The same value will be used by the Elasticsearch JVM.
+      minHeap: "512m"

     # -- Specifies the standard Kubernetes resource requests and/or limits for the Bitbucket container.
     # It is important that if the memory resources are specified here, they must allow for the size of the Bitbucket JVM.
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
+    #    cpu: "1"
     #    memory: "2G"
+      requests:
+        cpu: "2" # -- If changing the cpu value update additional JVM arg 'ActiveProcessorCount' below
+        memory: "2G"

   # -- Specifies a list of additional arguments that can be passed to the Bitbucket JVM, e.g. system properties
   additionalJvmArgs:
-  #    - -Dfoo=bar
-  #    - -Dfruit=lemon
+    # -- The value defined for ActiveProcessorCount should correspond to that provided for 'container.requests.cpu'
+    # see: https://docs.oracle.com/en/java/javase/11/tools/java.html#GUID-3B1CE181-CD30-4178-9602-230B800D4FAE
+    - -XX:ActiveProcessorCount=2

   # -- Specifies a list of additional Java libraries that should be added to the Bitbucket container.
-  # Each item in the list should specify the name of the volume which contain the library, as well as the name of the
+  # Each item in the list should specify the name of the volume that contains the library, as well as the name of the
   # library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which
   # directory in the volume contains the library file.
   additionalLibraries: []
@@ -163,9 +167,11 @@ ingress:
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
@@ -176,9 +182,9 @@ ingress:
   tlsSecretName:

 fluentd:
-  # -- True if the fluentd sidecar should be added to each pod
+  # -- True if the Fluentd sidecar should be added to each pod
   enabled: false
-  # -- True if a custom config should be used for fluentd
+  # -- True if a custom config should be used for Fluentd
   customConfigFile: false
   # -- Custom fluent.conf file
   # fluent.conf: |
@@ -195,14 +201,14 @@ fluentd:
   #     tag bitbucket-access-logs
   #   </source>

-  # -- The name of the image containing the fluentd sidecar
+  # -- The name of the image containing the Fluentd sidecar
   imageName: fluent/fluentd-kubernetes-daemonset:v1.11.5-debian-elasticsearch7-1.2
   elasticsearch:
-    # -- True if fluentd should send all log events to an elasticsearch service.
+    # -- True if Fluentd should send all log events to an Elasticsearch service.
     enabled: true
-    # -- The hostname of the Elasticsearch service that fluentd should send logs to.
+    # -- The hostname of the Elasticsearch service that Fluentd should send logs to.
     hostname: elasticsearch
-  # -- pecify custom volumes to be added to fluentd container (e.g. more log sources)
+  # -- Specify custom volumes to be added to Fluentd container (e.g. more log sources)
   extraVolumes: []
   # - name: local-home
   #   mountPath: application-data/logs
@@ -225,7 +231,7 @@ volumes:
         requests:
           storage: 1Gi
     # -- When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes
-    # volume which will be used for the local-home volumes. If not defined, then defaults to an emptyDir volume.
+    # volume, which will be used for the local-home volumes. If not defined, then defaults to an emptyDir volume.
     customVolume: {}
     mountPath: "/var/atlassian/application-data/bitbucket"
   sharedHome:
@@ -251,11 +257,11 @@ volumes:
         requests:
           storage: 1Gi
     # -- When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes
-    # volume which will be used for the shared-home volume. If not defined, then defaults to an emptyDir (i.e. unshared) volume.
+    # volume, which will be used for the shared-home volume. If not defined, then defaults to an emptyDir (i.e. unshared) volume.
     customVolume: {}
     # -- Specifies the path in the Bitbucket container to which the shared-home volume will be mounted.
     mountPath: "/var/atlassian/application-data/shared-home"
-    # -- Specifies the sub-directory of the shared-home volume which will be mounted in to the Bitbucket container.
+    # -- Specifies the sub-directory of the shared-home volume that will be mounted in to the Bitbucket container.
     subPath:
     nfsPermissionFixer:
       # -- If enabled, this will alter the shared-home volume's root directory so that Bitbucket can write to it.
@@ -269,7 +275,7 @@ volumes:
       command:
   # -- Defines additional volumes that should be applied to all Bitbucket pods.
   # Note that this will not create any corresponding volume mounts;
-  # those needs to be defined in bitbucket.additionalVolumeMounts
+  # those need to be defined in bitbucket.additionalVolumeMounts
   additional: []

 # -- Standard Kubernetes node-selectors that will be applied to all Bitbucket pods
@@ -281,12 +287,12 @@ tolerations: []
 # -- Standard Kubernetes affinities that will be applied to all Bitbucket pods
 # Due to the performance requirements it is highly recommended to run all Bitbucket pods
 # in the same availability zone as your dedicated NFS server. To achieve this, you
-# able to define `affinity` and podAffinity` rules that will place all pods into the same zone
+# can define `affinity` and `podAffinity` rules that will place all pods into the same zone,
 # and therefore minimise the real distance between the application pods and the shared storage.
-# More specific documentation can be found official Affinity and Anti-affinity documentation
+# More specific documentation can be found in the official Affinity and Anti-affinity documentation:
 #  https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity
 #
-# This is an example how to ensure the pods are in the same zone as NFS server that is labeled with `role=nfs-server`:
+# This is an example on how to ensure the pods are in the same zone as NFS server that is labeled with `role=nfs-server`:
 #
 #   podAffinity:
 #    requiredDuringSchedulingIgnoredDuringExecution:
@@ -308,11 +314,11 @@ additionalInitContainers: []
 # -- Additional labels that should be applied to all resources
 additionalLabels: {}

-# -- Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into server container
+# -- Additional existing ConfigMaps and Secrets not managed by Helm, which should be mounted into server container
 # configMap and secret are two available types (camelCase is important!)
-# mountPath is a destination directory in a container and key is file name
-# name references existing ConfigMap or secret name. VolumeMount and Volumes are added with this name + index position,
-# for example custom-config-0, keystore-2
+# mountPath is a destination directory in a container, and key is the file name
+# name references existing ConfigMap or secret name. VolumeMount and Volumes are added with this name and the index position,
+# for example, custom-config-0, keystore-2
 additionalFiles: []
 #  - name: custom-config
 #    type: configMap
```

## 0.7.0

**Release date:** 2021-05-07

![AppVersion: 7.12.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.1-jdk11&color=success&logo=)
![Kubernetes: >=1.17.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.17.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Release 0.7.0 (#123)
* Update charts descriptors (#121)
* feat (fluentd) extra fluentd customization to (jira / confluence) helm charts (#95)
* Update application versions (#116)
* Rename Bitbucket
* DCKUBE-272: Expose Ehcache ports via statefulset
* Update documentation for 0.6.0 release
* DCKUBE-30: Example implementation for NFS Server (Bitbucket)
* DCKUBE-231: Max body size should be configurable
* DCKUBE-90: Additional details relating to the Ingress controller
* Introducing an easier way to mount secrets and configmaps (#102)
* Merge branch 'master' into dckube-131-nfs-fixer-removal
* DCKUBE-131: fix for initContainer being synthesized twice
* Merge branch 'master' into dckube-131-nfs-fixer-removal
* DCKUBE-131: Updates to template formatting and readme wording
* Merge pull request #98 from https://github.com/atlassian/data-center-helm-charts/dckube-131-nfs-fixer-removal
* DCKUBE-131: Alter NFS permissions via init container
* feat(range): Fix support for multiple plugins
* Update READMEs for 0.5.0 release
* Merge pull request #59 from https://github.com/atlassian/data-center-helm-charts/minor-cleanup-and-Azure-related-fixes
* DCNG-976 add fluentd sidecar to bitbucket chart
* Fix gid value
* OpenShift support (#56)
* DCNG-783 minor cleanup and Azure related fixes
* DCNG-926 setup NFSv3 server with lock support
* DCNG-945 Set up an NFS server with the Bitbucket server test
* DCNG-925 support for BYO NFS server
* default customVolume chart value to empty map , to avoid helm warning
* Add optional TLS to ingress spec
* DCNG-892 simplify config of https/http
* DCNG-892 update docs
* Merge remote-tracking branch 'origin/master' into DCNG-892
* DCNG-892 Configure the created ingress as nginx by default
* DCNG-892 Move ingress value structure up to top level
* DCNG-906 Set ELASTICSEARCH_ENABLED=false if ES base URL is defined
* DCNG-892 Add Ingress template to the Helm charts, and activate it for EKS testing
* DCNG-906 Add support for declaring ElasticSearch baseUrl and credentials in the bitbucket chart
* DCNG-927 Tweak doco for clarity
* DCNG-927 Allow Tomcat ingress https/secure config to be changed for Jira/Confluence, and make consistent with Bitbucket
* DCNG-921 add doco for enabling clustering
* DCNG-921 disable clustering by defaault
* DCNG-918 Make bitbucket sysadmin credentials secret optional
* DCNG-918 Make bitbucket.proxy.fqdn an optional value
* DCNG-913 Make bitbucket/confluence license secret optional
* DCNG-914 make jira/confluence DB config values fully optional
* DCNG-914 make bitbucket DB config values fully optional
* DCNG-893 move emptyDir volume defaults back into the templates
* DCNG-893 Make localHome/sharedHome configuration more consistent by adding an optional shared-home PVC to the chart
* rename localHome.persistentVolumeClaim.enabled to .create
* DCNG-893 update documentation
* DCNG-893 rework how volumes are customised in the Bitbucket chart
* DCNG-893 disable PVs by default on Bitbucket and Confluence
* DCNG-898 add a series of unit tests for the serviceAccount and image config rendering
* DCNG-899 Added service account to db-connectivity-test
* DCNG-897 Use "before-hook-creation,hook-succeeded" deletion policy
* Merge remote-tracking branch 'origin/master' into DCNG-897
* DCNG-894 Add a ServiceAccount, ClusterRole and ClusterRoleBinding to the Bitbucket chart
* DCNG-897 Add hook-delete-policy to chart tests and nfs-fixer job
* DCNG-880 Added support for custom builds in kubeVersion
* DCNG-853 Fixed kubeVersion
* DCNG-853 Updated products' charts
* DCNG-856 add -n to each command in NOTES.txt
* DCNG-856 Add NOTES.txt
* DCNG-849 avoid incorrect rendering for empty  additionalEnvironmentVariables
* DCNG-849 check for the presense of additionalLabels so we don't render an empty {}
* Merge pull request #6 from https://github.com/atlassian/data-center-helm-charts/DCNG-849
* DCNG-850 add extension point for additional environment variables
* DCNG-848 add extension point for additional labels
* Merge pull request #2 from https://github.com/atlassian/data-center-helm-charts/DCNG-867
* Merge pull request #3 from https://github.com/atlassian/data-center-helm-charts/DCNG-848
* DCNG-866 Replace hardcoded image pull policy with value placeholder
* DCNG-848 Add support for additional volumes and volume mounts
* DCNG-867 Document the presense of  bitbucket.additionalJvmArgs

### Default value changes

```diff
diff --git a/src/main/charts/bitbucket/values.yaml b/src/main/charts/bitbucket/values.yaml
index 1df2526..bb7a568 100644
--- a/src/main/charts/bitbucket/values.yaml
+++ b/src/main/charts/bitbucket/values.yaml
@@ -7,17 +7,40 @@ image:
   # -- The docker image tag to be used. Defaults to the Chart appVersion.
   tag: ""

-# -- Specifies which serviceAccount to use for the pods. If not specified, the kubernetes default will be used.
-serviceAccountName:
+serviceAccount:
+  # -- Specifies the name of the ServiceAccount to be used by the pods.
+  # If not specified, but the the "serviceAccount.create" flag is set, then the ServiceAccount name will be auto-generated,
+  # otherwise the 'default' ServiceAccount will be used.
+  name:
+  # -- true if a ServiceAccount should be created, or false if it already exists
+  create: true
+  # -- The list of image pull secrets that should be added to the created ServiceAccount
+  imagePullSecrets: []
+  clusterRole:
+    # -- Specifies the name of the ClusterRole that will be created if the "serviceAccount.clusterRole.create" flag is set.
+    # If not specified, a name will be auto-generated.
+    name:
+    # -- true if a ClusterRole should be created, or false if it already exists
+    create: true
+  clusterRoleBinding:
+    # -- Specifies the name of the ClusterRoleBinding that will be created if the "serviceAccount.clusterRoleBinding.create" flag is set
+    # If not specified, a name will be auto-generated.
+    name:
+    # -- true if a ClusterRoleBinding should be created, or false if it already exists
+    create: true

 database:
   # -- The JDBC URL of the database to be used by Bitbucket, e.g. jdbc:postgresql://host:port/database
+  # If not specified, then it will need to be provided via browser during initial startup.
   url:
   # -- The Java class name of the JDBC driver to be used, e.g. org.postgresql.Driver
+  # If not specified, then it will need to be provided via browser during initial startup.
   driver:
   credentials:
     # -- The name of the Kubernetes Secret that contains the database login credentials.
-    secretName: bitbucket-database-credentials
+    # If specified, then the credentials will be automatically populated during Bitbucket setup.
+    # Otherwise, they will need to be provided via the browser after initial startup.
+    secretName:
     # -- The key in the Secret used to store the database login username
     usernameSecretKey: username
     # -- The key in the Secret used to store the database login password
@@ -25,26 +48,34 @@ database:

 bitbucket:
   service:
-    # -- The port on which the Jira Kubernetes service will listen
+    # -- The port on which the Bitbucket Kubernetes service will listen
     port: 80
-    # -- The type of Kubernetes service to use for Jira
+    # -- The type of Kubernetes service to use for Bitbucket
     type: ClusterIP
-  # -- The GID used by the Bitbucket docker image
-  gid: "2003"
+  # -- Enable or disable security context in StatefulSet template spec. Enabled by default with UID 2003.
+  # -- Disable when deploying to OpenShift, unless anyuid policy is attached to service account
+  securityContext:
+    enabled: true
+    # -- The GID used by the Bitbucket docker image
+    gid: "2003"
   ports:
     http: 7990
     ssh: 7999
     hazelcast: 5701

   license:
-    # -- The name of the Kubernetes Secret which contains the Bitbucket license key
-    secretName: bitbucket-license
+    # -- The name of the Kubernetes Secret which contains the Bitbucket license key.
+    # If specified, then the license will be automatically populated during Bitbucket setup.
+    # Otherwise, it will need to be provided via the browser after initial startup.
+    secretName:
     # -- The key in the Kubernetes Secret which contains the Bitbucket license key
     secretKey: license-key

   sysadminCredentials:
     # -- The name of the Kubernetes Secret which contains the Bitbucket sysadmin credentials
-    secretName: bitbucket-sysadmin-credentials
+    # If specified, then these will be automatically populated during Bitbucket setup.
+    # Otherwise, they will need to be provided via the browser after initial startup.
+    secretName:
     # -- The key in the Kubernetes Secret which contains the sysadmin username
     usernameSecretKey: username
     # -- The key in the Kubernetes Secret which contains the sysadmin password
@@ -54,20 +85,28 @@ bitbucket:
     # -- The key in the Kubernetes Secret which contains the sysadmin email address
     emailAddressSecretKey: emailAddress

-  proxy:
-    # -- The fully-qualified domain name of the ingress
-    fqdn:
-    # -- The port number of the ingress
-    port: 443
-    # -- note that, if present, the value of x-forwarded-proto header will trump this setting
-    scheme: https
-    secure: true
+  clustering:
+    # -- Set to true if Data Center clustering should be enabled
+    # This will automatically configure cluster peer discovery between cluster nodes.
+    enabled: false
+
+  elasticSearch:
+    # -- The base URL of the external ElasticSearch instance to be used.
+    # If this is defined, then Bitbucket will disable its internal ElasticSearch instance.
+    baseUrl:
+    credentials:
+      # -- The name of the Kubernetes Secret that contains the ElasticSearch credentials.
+      secretName:
+      # -- The key in the the Kubernetes Secret that contains the ElasticSearch username.
+      usernameSecreyKey: username
+      # -- The key in the the Kubernetes Secret that contains the ElasticSearch password.
+      passwordSecretKey: password

   resources:
     jvm:
-      # -- The maximum amount of heap memory that will be used by the Bitbucket JVM
+      # -- The maximum amount of heap memory that will be used by the Bitbucket JVM. The same value will be used by the ElasticSearch JVM.
       maxHeap: "1g"
-      # -- The minimum amount of heap memory that will be used by the Bitbucket JVM
+      # -- The minimum amount of heap memory that will be used by the Bitbucket JVM. The same value will be used by the ElasticSearch JVM.
       minHeap: "1g"

     # -- Specifies the standard Kubernetes resource requests and/or limits for the Bitbucket container.
@@ -82,6 +121,11 @@ bitbucket:
     #    cpu: "4"
     #    memory: "2G"

+  # -- Specifies a list of additional arguments that can be passed to the Bitbucket JVM, e.g. system properties
+  additionalJvmArgs:
+  #    - -Dfoo=bar
+  #    - -Dfruit=lemon
+
   # -- Specifies a list of additional Java libraries that should be added to the Bitbucket container.
   # Each item in the list should specify the name of the volume which contain the library, as well as the name of the
   # library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which
@@ -99,36 +143,134 @@ bitbucket:
   #    subDirectory:
   #    fileName:

+  # -- Defines any additional volumes mounts for the Bitbucket container.
+  # These can refer to existing volumes, or new volumes can be defined in volumes.additional.
+  additionalVolumeMounts: []
+
+  # -- Defines any additional environment variables to be passed to the Bitbucket container.
+  # See https://hub.docker.com/r/atlassian/bitbucket-server for supported variables.
+  additionalEnvironmentVariables: []
+
+ingress:
+  # -- True if an Ingress Resource should be created.
+  create: false
+  # -- True if the created Ingress Resource is to use the Kubernetes ingress-nginx controller:
+  # https://kubernetes.github.io/ingress-nginx/
+  # This will populate the Ingress Resource with annotations for the Kubernetes ingress-nginx controller.
+  # Set to false if a different controller is to be used, in which case the appropriate annotations for that
+  # controller need to be specified.
+  nginx: true
+  # -- The max body size to allow. Requests exceeding this size will result
+  # in an 413 error being returned to the client.
+  # https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#custom-max-body-size
+  maxBodySize: 10m
+  # -- The fully-qualified hostname of the Ingress Resource.
+  host:
+  # -- The custom annotations that should be applied to the Ingress
+  # Resource when not using the Kubernetes ingress-nginx controller.
+  annotations: {}
+  # -- True if the browser communicates with the application over HTTPS.
+  https: true
+  # -- Secret that contains a TLS private key and certificate.
+  # Optional if Ingress Controller is configured to use one secret for all ingresses
+  tlsSecretName:
+
+fluentd:
+  # -- True if the fluentd sidecar should be added to each pod
+  enabled: false
+  # -- True if a custom config should be used for fluentd
+  customConfigFile: false
+  # -- Custom fluent.conf file
+  # fluent.conf: |
+  fluentdCustomConfig: {}
+  # fluent.conf: |
+  #   <source>
+  #     @type tail
+  #     <parse>
+  #     @type multiline
+  #     format_firstline /\d{4}-\d{1,2}-\d{1,2}/
+  #     </parse>
+  #     path /application-data/logs/atlassian-bitbucket-access.log*
+  #     pos_file /tmp/bitbucketlog.pos
+  #     tag bitbucket-access-logs
+  #   </source>
+
+  # -- The name of the image containing the fluentd sidecar
+  imageName: fluent/fluentd-kubernetes-daemonset:v1.11.5-debian-elasticsearch7-1.2
+  elasticsearch:
+    # -- True if fluentd should send all log events to an elasticsearch service.
+    enabled: true
+    # -- The hostname of the Elasticsearch service that fluentd should send logs to.
+    hostname: elasticsearch
+  # -- pecify custom volumes to be added to fluentd container (e.g. more log sources)
+  extraVolumes: []
+  # - name: local-home
+  #   mountPath: application-data/logs
+  #   subPath: log
+  #   readOnly: true
+
 # -- Specify custom annotations to be added to all Bitbucket pods
 podAnnotations: {}
 #  "name": "value"

 volumes:
   localHome:
-    # -- Specifies the name of the storage class that should be used for the Bitbucket local-home volume
-    storageClassName:
-    # -- Specifies the standard Kubernetes resource requests and/or limits for the Bitbucket local-home volume.
-    resources:
-      requests:
-        storage: 1Gi
+    persistentVolumeClaim:
+      # -- If true, then a PersistentVolumeClaim will be created for each local-home volume.
+      create: false
+      # -- Specifies the name of the storage class that should be used for the local-home volume claim.
+      storageClassName:
+      # -- Specifies the standard Kubernetes resource requests and/or limits for the local-home volume claims.
+      resources:
+        requests:
+          storage: 1Gi
+    # -- When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes
+    # volume which will be used for the local-home volumes. If not defined, then defaults to an emptyDir volume.
+    customVolume: {}
     mountPath: "/var/atlassian/application-data/bitbucket"
   sharedHome:
+    persistentVolume:
+      # -- If true, then a PersistentVolume will be created for the shared-home volume.
+      create: false
+      # -- Addtional options used when mounting the volume
+      mountOptions: []
+      nfs:
+        # -- The address of the NFS server. It needs to be resolveable by the kubelet, so consider using an IP address.
+        server: ""
+        # -- Specifies the path exported by the NFS server, used in the mount command
+        path: ""
+    persistentVolumeClaim:
+      # -- If true, then a PersistentVolumeClaim will be created for the shared-home volume.
+      create: false
+      # -- Specifies the name of the storage class that should be used for the shared-home volume claim.
+      storageClassName:
+      # -- Specifies the name of the persistent volume to claim
+      volumeName:
+      # -- Specifies the standard Kubernetes resource requests and/or limits for the shared-home volume claims.
+      resources:
+        requests:
+          storage: 1Gi
+    # -- When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes
+    # volume which will be used for the shared-home volume. If not defined, then defaults to an emptyDir (i.e. unshared) volume.
+    customVolume: {}
     # -- Specifies the path in the Bitbucket container to which the shared-home volume will be mounted.
     mountPath: "/var/atlassian/application-data/shared-home"
     # -- Specifies the sub-directory of the shared-home volume which will be mounted in to the Bitbucket container.
     subPath:
-    # -- The name of the PersistentVolumeClaim which will be used for the shared-home volume
-    volumeClaimName: bitbucket-shared-home
     nfsPermissionFixer:
       # -- If enabled, this will alter the shared-home volume's root directory so that Bitbucket can write to it.
       # This is a workaround for a Kubernetes bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260
-      enabled: true
+      enabled: false
       # -- The path in the initContainer where the shared-home volume will be mounted
       mountPath: /shared-home
       # -- By default, the fixer will change the group ownership of the volume's root directory to match the Bitbucket
       # container's GID (2003), and then ensures the directory is group-writeable. If this is not the desired behaviour,
       # command used can be specified here.
       command:
+  # -- Defines additional volumes that should be applied to all Bitbucket pods.
+  # Note that this will not create any corresponding volume mounts;
+  # those needs to be defined in bitbucket.additionalVolumeMounts
+  additional: []

 # -- Standard Kubernetes node-selectors that will be applied to all Bitbucket pods
 nodeSelector: {}
@@ -137,6 +279,24 @@ nodeSelector: {}
 tolerations: []

 # -- Standard Kubernetes affinities that will be applied to all Bitbucket pods
+# Due to the performance requirements it is highly recommended to run all Bitbucket pods
+# in the same availability zone as your dedicated NFS server. To achieve this, you
+# able to define `affinity` and podAffinity` rules that will place all pods into the same zone
+# and therefore minimise the real distance between the application pods and the shared storage.
+# More specific documentation can be found official Affinity and Anti-affinity documentation
+#  https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity
+#
+# This is an example how to ensure the pods are in the same zone as NFS server that is labeled with `role=nfs-server`:
+#
+#   podAffinity:
+#    requiredDuringSchedulingIgnoredDuringExecution:
+#      - labelSelector:
+#          matchExpressions:
+#            - key: role
+#              operator: In
+#              values:
+#                - nfs-server # needs to be the same value as NFS server deployment
+#        topologyKey: topology.kubernetes.io/zone
 affinity: {}

 # -- Additional container definitions that will be added to all Bitbucket pods
@@ -144,3 +304,26 @@ additionalContainers: []

 # -- Additional initContainer definitions that will be added to all Bitbucket pods
 additionalInitContainers: []
+
+# -- Additional labels that should be applied to all resources
+additionalLabels: {}
+
+# -- Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into server container
+# configMap and secret are two available types (camelCase is important!)
+# mountPath is a destination directory in a container and key is file name
+# name references existing ConfigMap or secret name. VolumeMount and Volumes are added with this name + index position,
+# for example custom-config-0, keystore-2
+additionalFiles: []
+#  - name: custom-config
+#    type: configMap
+#    key: log4j.properties
+#    mountPath:  /var/atlassian
+#  - name: custom-config
+#    type: configMap
+#    key: web.xml
+#    mountPath: /var/atlassian
+#  - name: keystore
+#    type: secret
+#    key: keystore.jks
+#    mountPath: /var/ssl
+
```

## 0.1.0

**Release date:** 2020-11-04

![AppVersion: 7.7.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.7.0-jdk11&color=success&logo=)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* OSR-523 Snapshot of helm charts and test code from internal repo

### Default value changes

```diff
# -- The initial number of pods that should be started at deployment of Bitbucket.
replicaCount: 1

image:
  repository: atlassian/bitbucket-server
  pullPolicy: IfNotPresent
  # -- The docker image tag to be used. Defaults to the Chart appVersion.
  tag: ""

# -- Specifies which serviceAccount to use for the pods. If not specified, the kubernetes default will be used.
serviceAccountName:

database:
  # -- The JDBC URL of the database to be used by Bitbucket, e.g. jdbc:postgresql://host:port/database
  url:
  # -- The Java class name of the JDBC driver to be used, e.g. org.postgresql.Driver
  driver:
  credentials:
    # -- The name of the Kubernetes Secret that contains the database login credentials.
    secretName: bitbucket-database-credentials
    # -- The key in the Secret used to store the database login username
    usernameSecretKey: username
    # -- The key in the Secret used to store the database login password
    passwordSecretKey: password

bitbucket:
  service:
    # -- The port on which the Jira Kubernetes service will listen
    port: 80
    # -- The type of Kubernetes service to use for Jira
    type: ClusterIP
  # -- The GID used by the Bitbucket docker image
  gid: "2003"
  ports:
    http: 7990
    ssh: 7999
    hazelcast: 5701

  license:
    # -- The name of the Kubernetes Secret which contains the Bitbucket license key
    secretName: bitbucket-license
    # -- The key in the Kubernetes Secret which contains the Bitbucket license key
    secretKey: license-key

  sysadminCredentials:
    # -- The name of the Kubernetes Secret which contains the Bitbucket sysadmin credentials
    secretName: bitbucket-sysadmin-credentials
    # -- The key in the Kubernetes Secret which contains the sysadmin username
    usernameSecretKey: username
    # -- The key in the Kubernetes Secret which contains the sysadmin password
    passwordSecretKey: password
    # -- The key in the Kubernetes Secret which contains the sysadmin display name
    displayNameSecretKey: displayName
    # -- The key in the Kubernetes Secret which contains the sysadmin email address
    emailAddressSecretKey: emailAddress

  proxy:
    # -- The fully-qualified domain name of the ingress
    fqdn:
    # -- The port number of the ingress
    port: 443
    # -- note that, if present, the value of x-forwarded-proto header will trump this setting
    scheme: https
    secure: true

  resources:
    jvm:
      # -- The maximum amount of heap memory that will be used by the Bitbucket JVM
      maxHeap: "1g"
      # -- The minimum amount of heap memory that will be used by the Bitbucket JVM
      minHeap: "1g"

    # -- Specifies the standard Kubernetes resource requests and/or limits for the Bitbucket container.
    # It is important that if the memory resources are specified here, they must allow for the size of the Bitbucket JVM.
    # That means the maximum heap size, the reserved code cache size, plus other JVM overheads, must be accommodated.
    # Allowing for (maxHeap+codeCache)*1.5 would be an example.
    container: {}
    #  limits:
    #    cpu: "4"
    #    memory: "2G"
    #  requests:
    #    cpu: "4"
    #    memory: "2G"

  # -- Specifies a list of additional Java libraries that should be added to the Bitbucket container.
  # Each item in the list should specify the name of the volume which contain the library, as well as the name of the
  # library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which
  # directory in the volume contains the library file.
  additionalLibraries: []
  #  - volumeName:
  #    subDirectory:
  #    fileName:

  # -- Specifies a list of additional Bitbucket plugins that should be added to the Bitbucket container.
  # These are specified in the same manner as the additionalLibraries field, but the files will be loaded
  # as bundled plugins rather than as libraries.
  additionalBundledPlugins: []
  #  - volumeName:
  #    subDirectory:
  #    fileName:

# -- Specify custom annotations to be added to all Bitbucket pods
podAnnotations: {}
#  "name": "value"

volumes:
  localHome:
    # -- Specifies the name of the storage class that should be used for the Bitbucket local-home volume
    storageClassName:
    # -- Specifies the standard Kubernetes resource requests and/or limits for the Bitbucket local-home volume.
    resources:
      requests:
        storage: 1Gi
    mountPath: "/var/atlassian/application-data/bitbucket"
  sharedHome:
    # -- Specifies the path in the Bitbucket container to which the shared-home volume will be mounted.
    mountPath: "/var/atlassian/application-data/shared-home"
    # -- Specifies the sub-directory of the shared-home volume which will be mounted in to the Bitbucket container.
    subPath:
    # -- The name of the PersistentVolumeClaim which will be used for the shared-home volume
    volumeClaimName: bitbucket-shared-home
    nfsPermissionFixer:
      # -- If enabled, this will alter the shared-home volume's root directory so that Bitbucket can write to it.
      # This is a workaround for a Kubernetes bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260
      enabled: true
      # -- The path in the initContainer where the shared-home volume will be mounted
      mountPath: /shared-home
      # -- By default, the fixer will change the group ownership of the volume's root directory to match the Bitbucket
      # container's GID (2003), and then ensures the directory is group-writeable. If this is not the desired behaviour,
      # command used can be specified here.
      command:

# -- Standard Kubernetes node-selectors that will be applied to all Bitbucket pods
nodeSelector: {}

# -- Standard Kubernetes tolerations that will be applied to all Bitbucket pods
tolerations: []

# -- Standard Kubernetes affinities that will be applied to all Bitbucket pods
affinity: {}

# -- Additional container definitions that will be added to all Bitbucket pods
additionalContainers: []

# -- Additional initContainer definitions that will be added to all Bitbucket pods
additionalInitContainers: []
```

---
Autogenerated from Helm Chart and git history using [helm-changelog](https://github.com/mogensen/helm-changelog)
