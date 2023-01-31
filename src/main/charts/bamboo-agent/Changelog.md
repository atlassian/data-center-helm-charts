# Change Log

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

* Bamboo Agent updated to 9.0.0 (#455)

## 1.5.0

**Release date:** 2022-07-14

![AppVersion: 8.2.4](https://img.shields.io/static/v1?label=AppVersion&message=8.2.4&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Bamboo Agent updated to 8.2.4 (#430)

## 1.4.0

**Release date:** 2022-05-25

![AppVersion: 8.2.3](https://img.shields.io/static/v1?label=AppVersion&message=8.2.3&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Make pod securityContext optional (#389)
* Update Bamboo agent version to 8.2.3 (#412)


## 1.3.0

**Release date:** 2022-03-24

![AppVersion: 8.1.3](https://img.shields.io/static/v1?label=AppVersion&message=8.1.3&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

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


## 1.0.0

**Release date:** 2022-01-11

This is the first officially supported version of the Helm chart.

![AppVersion: 8.1.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Support for defining podLabels (#366)
* Support for defining podAnnotations (#341)

## 0.0.2

**Release date:** 2021-12-17

![AppVersion: 8.1.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* DCKUBE-738: Add topologySpreadConstraints to products (#351)
* Add additionalPorts for the Bamboo Agent Deployment (#353)
* BAMBK8S-86: Initial agent support (#335)

## 0.0.1

Initial release
