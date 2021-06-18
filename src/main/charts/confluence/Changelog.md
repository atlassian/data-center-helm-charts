# Change Log

## 0.11.0 

**Release date:** 2021-06-09

![AppVersion: 7.12.2-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.2-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-348: Warning of absent persistent volume (#169) 
* DCKUBE-307: Do not print logs when testing helm installation. (#168) 
* DCKUBE-308: Print service URL after installing helm chart (#157) 
* DCKUBE-331: Mount additional libraries in DB connectivity pod. (#162) 
* DCKUBE-282: Update icons to SVG (#164) 
* DCKUBE-322: Revert previous enabling of Synchrony by default for now … (#160) 
* DCKUBE-322: Add resources stanza for Synchrony and inject values into startup (#151) 

### Default value changes

```diff
diff --git a/src/main/charts/confluence/values.yaml b/src/main/charts/confluence/values.yaml
index a39d4ae..8e148f3 100644
--- a/src/main/charts/confluence/values.yaml
+++ b/src/main/charts/confluence/values.yaml
@@ -183,6 +183,18 @@ synchrony:
     periodSeconds: 1
     # -- The number of consecutive failures of the Synchrony container readiness probe before the pod fails readiness checks
     failureThreshold: 30
+  resources:
+    jvm:
+      # -- The minimum amount of heap memory that will be used by the Synchrony JVM
+      minHeap: "1g"
+      # -- The maximum amount of heap memory that will be used by the Synchrony JVM
+      maxHeap: "2g"
+      # -- The memory allocated for the Synchrony stack
+      stackSize: "2048k"
+    container: 
+      requests:
+        cpu: "2"
+        memory: "2.5G"
   # -- The base URL of the Synchrony service.
   # This will be the URL that users' browsers will be given to communicate with Synchrony, as well as the URL that the
   # Confluence service will use to communicate directly with Synchrony, so the URL must be resovable both from inside and
```

## 0.10.0 

**Release date:** 2021-06-01

![AppVersion: 7.12.0-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=7.12.0-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Version 0.10.0 
* DCKUBE-332: Update the minimal supported kubernetes version v1.19 (#154) 

### Default value changes

```diff
# No changes in this release
```
