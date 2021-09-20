# Change Log


## 0.14.0

![AppVersion: 8.13.9-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.9-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-529: Update Jira version to 8.13.9-jdk11 (#212)


## 0.13.0

![AppVersion: 8.13.7-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.7-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-474: Added Jira Proxy information to helm chart (#202)
* DCKUBE-54: Volume docs updates (#188)


## 0.12.0 

![AppVersion: 8.13.7-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.7-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-437: Wording improvement for warning in NOTES when PV is not used (#199) 
* DCKUBE-390: Improve readability of Jira values.yaml file (#179) 
* Defining the following values in the helpers template for each chart, to allow template overrides: (#173)

### Default value changes

There has been major improvement in the documentation for the keys in `values.yaml` file but there isn't any functional change. 

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
