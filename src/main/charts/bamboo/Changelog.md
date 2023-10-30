# Change Log

## 1.16.6

**Release date:** 2023-10-30

![AppVersion: 9.4.0](https://img.shields.io/static/v1?label=AppVersion&message=9.4.0&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* JMX container resources (#707)

## 1.16.5

**Release date:** 2023-10-24

![AppVersion: 9.3.4](https://img.shields.io/static/v1?label=AppVersion&message=9.3.4&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Fix custom jmx config (#699)
* Copy cacerts first, then import certs (#696)

## 1.16.4

**Release date:** 2023-10-11

![AppVersion: 9.3.3](https://img.shields.io/static/v1?label=AppVersion&message=9.3.3&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update Helm chart version

## 1.16.3

**Release date:** 2023-10-11

![AppVersion: 9.3.3](https://img.shields.io/static/v1?label=AppVersion&message=9.3.3&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update Helm chart version

## 1.16.2

**Release date:** 2023-10-8

![AppVersion: 9.3.3](https://img.shields.io/static/v1?label=AppVersion&message=9.3.3&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add quote to podAnnotation templating (#678)

## 1.16.1

**Release date:** 2023-9-20

![AppVersion: 9.3.3](https://img.shields.io/static/v1?label=AppVersion&message=9.3.3&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update Helm chart version

## 1.16.0

**Release date:** 2023-9-18

![AppVersion: 9.3.3](https://img.shields.io/static/v1?label=AppVersion&message=9.3.3&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Make securityContext configurable for jmx exporter init container (#670)
* Add user provided certificates to the default Java truststore (#663)

## 1.15.3

**Release date:** 2023-8-28

![AppVersion: 9.3.2](https://img.shields.io/static/v1?label=AppVersion&message=9.3.2&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Decouple server and jmx annotations (#654)
* Disable startup probes by default (#653)

## 1.15.2

**Release date:** 2023-8-22

![AppVersion: 9.3.2](https://img.shields.io/static/v1?label=AppVersion&message=9.3.2&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add defaultMode to additionalConfigMaps (#647)

## 1.15.1

**Release date:** 2023-8-17

![AppVersion: 9.3.2](https://img.shields.io/static/v1?label=AppVersion&message=9.3.2&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Make startup probes optional (#646)

## 1.15.0

**Release date:** 2023-8-7

![AppVersion: 9.3.1](https://img.shields.io/static/v1?label=AppVersion&message=9.3.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add PodDisruptionBudget to Atlassian DC Helm Charts (#636)
* Add annotations to Grafana dashboards ConfigMaps (#637)
* Add additional ConfigMaps to Helm Charts (#635)

## 1.14.1

**Release date:** 2023-7-26

![AppVersion: 9.3.1](https://img.shields.io/static/v1?label=AppVersion&message=9.3.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Disable liveness probes by default, make timeoutSeconds configurable (#630)

## 1.14.0

**Release date:** 2023-7-25

![AppVersion: 9.3.1](https://img.shields.io/static/v1?label=AppVersion&message=9.3.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add liveness probes, make readinessProbes configurable (#626)
* make sharedHome.permissionFix.command helper to be per-product (#622)

## 1.13.1

**Release date:** 2023-6-28

![AppVersion: 9.3.0](https://img.shields.io/static/v1?label=AppVersion&message=9.3.0&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Run jmx init container as root: get fix from common chart ver 1.2.3 (#608)

## 1.13.0

**Release date:** 2023-6-13

![AppVersion: 9.3.0](https://img.shields.io/static/v1?label=AppVersion&message=9.3.0&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update appVersions for DC apps (#599)
* Update Grafana dashboards for DC apps (#589)
* Bamboo dashboard (#588)
* Make sessionAffinity configurable in service spec (#582)
* Add optional ServiceMonitors to DC Helm Charts (#573)
* Enable JMS traffic via Service (#570)
* Expose JMX beans on http endpoint (#562)

## 1.12.0

**Release date:** 2023-4-18

![AppVersion: 9.2.1](https://img.shields.io/static/v1?label=AppVersion&message=9.2.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add priorityClassName to pod spec (#557)
* Add hostAliases for all DC products (#556)

## 1.11.0

**Release date:** 2023-3-22

![AppVersion: 9.2.1](https://img.shields.io/static/v1?label=AppVersion&message=9.2.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update appVersions for DC apps (#531)

## 1.10.0

**Release date:** 2023-2-20

![AppVersion: 9.2.1](https://img.shields.io/static/v1?label=AppVersion&message=9.2.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Add Bitbucket Mesh to Bitbucket Helm chart (#501)

## 1.9.1

**Release date:** 2023-2-16

![AppVersion: 9.2.1](https://img.shields.io/static/v1?label=AppVersion&message=9.2.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Fix artifact hub annotation yaml

## 1.9.0

**Release date:** 2023-2-15

![AppVersion: 9.2.1](https://img.shields.io/static/v1?label=AppVersion&message=9.2.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


## 1.8.1

**Release date:** 2022-12-12

![AppVersion: 9.0.1](https://img.shields.io/static/v1?label=AppVersion&message=9.0.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Fix artifacthub.io annotations

## 1.8.0

**Release date:** 2022-12-9

![AppVersion: 9.0.1](https://img.shields.io/static/v1?label=AppVersion&message=9.0.1&color=success&logo=)
![Kubernetes: >=1.21.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.21.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* CLIP-1644: Stop supporting 1.19-1.20 k8s (#486)
* Update appVersion to 9.0.1
* Use `ingress.https` flag to enable tls in ingress (#487)

## 1.7.1

**Release date:** 2022-10-26

![AppVersion: 9.0.0](https://img.shields.io/static/v1?label=AppVersion&message=9.0.0&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Patch release to fix Artifacthub metadata

## 1.7.0

**Release date:** 2022-10-25

![AppVersion: 9.0.0](https://img.shields.io/static/v1?label=AppVersion&message=9.0.0&color=success&logo=)
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

![AppVersion: 9.0.0](https://img.shields.io/static/v1?label=AppVersion&message=9.0.0&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Update Bamboo version to 9.0.0 (#455)
* Improved documentation (#448, #440)


## 1.5.0

**Release date:** 2022-07-14

![AppVersion: 8.2.4](https://img.shields.io/static/v1?label=AppVersion&message=8.2.4&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Fix: Use the custom ports for Bamboo service (#419)
* Update Bamboo version to 8.2.4 (#430)

## 1.4.0

**Release date:** 2022-05-25

![AppVersion: 8.2.3](https://img.shields.io/static/v1?label=AppVersion&message=8.2.3&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Make pod securityContext optional (#389)
* Support for configuring ingress proxy settings via values.yaml (#402)
* Update Bamboo version to 8.2.3 (#412)

## 1.3.0

**Release date:** 2022-03-24

![AppVersion: 8.1.3](https://img.shields.io/static/v1?label=AppVersion&message=8.1.3&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* TFKUBE-384: ATL_BASE_URL should be appropriately set when ingress.path is supplied (#391)
* Update Bamboo version to 8.1.3 (#396)

## 1.2.0

**Release date:** 2022-02-14

![AppVersion: 8.1.2-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* DCD-1452: Updated appVersion to the latest product LTS version. (#378)
* Added end-to-end test for Bamboo Helm chart using Terraform (#375)
* Improvements on [documentation](https://github.com/atlassian/data-center-helm-charts/) (#370)
* Updated Atlassian charts to use common definitions (#303)
* Added service account annotation (#363)
* Added new feature additionalVolumeClaimTemplates and provided example in documentation (#334, #368)


## 1.0.0

**Release date:** 2022-01-11

This is the first officially supported version of the Helm chart.

![AppVersion: 8.1.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Support for defining podLabels (#366)
* Support for defining loadBalancerIP (#365)
* Support for defining podAnnotations (#341)

## 0.0.2

**Release date:** 2021-11-03

![AppVersion: 8.1.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* BAMBK8S-194: Support for unattended setups (#359)
* DCKUBE-738: Add topologySpreadConstraints to products (#351)
* BAMBK8S-129: Bamboo pre-seeding configuration (#349)
* BAMBK8S-86: Initial agent support (#335)
* Add additionalPorts for the Bamboo StatefulSet (#353)

## 0.0.1

Initial release
