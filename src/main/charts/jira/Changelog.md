# Change Log

## 0.11.0 

**Release date:** 2021-06-09

![AppVersion: 8.13.7-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.7-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-348: Warning of absent persistent volume (#169) 
* DCKUBE-307: Do not print logs when testing helm installation. (#168) 
* DCKUBE-308: Print service URL after installing helm chart (#157) 
* DCKUBE-282: Update icons to SVG (#164) 

### Default value changes

```diff
diff --git a/src/main/charts/jira/values.yaml b/src/main/charts/jira/values.yaml
index 2ec894b..0c3a003 100644
--- a/src/main/charts/jira/values.yaml
+++ b/src/main/charts/jira/values.yaml
@@ -46,7 +46,7 @@ jira:
     port: 80
     # -- The type of Kubernetes service to use for Jira
     type: ClusterIP
-    # -- The Tomcat context path that Confluence will use. The ATL_TOMCAT_CONTEXTPATH will be set automatically
+    # -- The Tomcat context path that Jira will use. The ATL_TOMCAT_CONTEXTPATH will be set automatically
     contextPath:
   # -- Enable or disable security context in StatefulSet template spec. Enabled by default with UID 2001.
   # -- Disable when deploying to OpenShift, unless anyuid policy is attached to a service account
```

## 0.10.0 

**Release date:** 2021-06-01

![AppVersion: 8.13.6-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.6-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* Version 0.10.0 
* DCKUBE-332: Update the minimal supported kubernetes version v1.19 (#154) 

### Default value changes

```diff
# No changes in this release
```
