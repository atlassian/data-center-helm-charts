# Change Log

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
