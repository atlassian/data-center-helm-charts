# Change Log

## 1.4.0

**Release date:** TBD

![AppVersion: 7.13.5](https://img.shields.io/static/v1?label=AppVersion&message=7.13.5&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Support for configuring ingress proxy settings via values.yaml (#402)

## 1.3.0

**Release date:** 2022-03-23

![AppVersion: 8.20.7](https://img.shields.io/static/v1?label=AppVersion&message=8.20.7&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCD-1455: Remove the unused Jira license value
* Update Jira version to 8.20.7 (#396)


## 1.2.0

**Release date:** 2022-02-14

![AppVersion: 8.20.5-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.20.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


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

![AppVersion: 8.20.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.20.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-721: Update version in Chart.yaml files 
* DCKUBE-733: Update the product versions (#345) 
* DCD-1380: Update Jira to 8.20 LTS (#339) 
* DCKUBE-739: Fix typos (#337) 
* DCKUBE-739: Make securityContext changes backward compatible (#332) 
* Roll Statefulset Pods if ConfigMap changes (#315) 
* DCKUBE-677: Make security context more flexible (#321) 
* DCKUBE-722: Enable configuring ingress.class name (#313) 
* BAMBK8S-47: Bamboo agent Helm chart (#316) 
* DCKUBE-717: Bind EHCACHE ports as environmental variables (#308) 
* DCKUBE-719: Cleanup redundant (not-used) jira.terminationGracePeriodSeconds (#306) 
* DCKUBE-678: Add schedulerName to StatefulSet (#301) 


## 1.0.0

This is the first officially supported version of the Helm chart.

![AppVersion: 8.13.9-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.9-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-621: Improvements to graceful shutdown (#282)
* DCKUBE-626: Update Jira to 8.19.0 (#286)
* Improved [documentation](https://github.com/atlassian/data-center-helm-charts/) (#275, #276, #277, #279, #280, #284, #285, #289, #290, #291, #293. #295)


## 0.16.0

![AppVersion: 8.13.9-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.9-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* DCKUBE-598: Enable NFS permission fixer by default (#241)
* DCKUBE-581: Enable configuration for SET_PERMISSIONS docker image variable (#261)
* DCKUBE-613: Configurable grace periods (#249)
* Improve [documentation](https://github.com/atlassian/data-center-helm-charts/) (#236, #243, #245, #252, #253, #256, #258, #260, #268, #270, #272)


## 0.15.0

![AppVersion: 8.13.9-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.13.9-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add Service annotations (#209)
* DCKUBE-453: Add support for providing a custom fluentd start command (#218)
* DCKUBE-435: Renamed the 'master' branch to 'main' and set it as default (#232)
* Update EKS cluster yaml example (#227)
* Improve [documentation](https://github.com/atlassian/data-center-helm-charts/) (#206, #222, #223, #228, #229, #231, #233, #235)


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
