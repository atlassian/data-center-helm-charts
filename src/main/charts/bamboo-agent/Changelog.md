# Change Log

## 1.4.0

**Release date:** TBD

![AppVersion: 7.13.5](https://img.shields.io/static/v1?label=AppVersion&message=7.13.5&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


## 1.3.0

**Release date:** 2022-23-03

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