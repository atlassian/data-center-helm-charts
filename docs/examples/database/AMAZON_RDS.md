# Creating an RDS DB instance
Instructions for creating an Amazon [RDS DB instance](https://aws.amazon.com/rds/)

## Pre-requisties
* An `AWS account`, `IAM user`, `VPC` (or [default VPC](https://docs.aws.amazon.com/vpc/latest/userguide/default-vpc.html)) and `security group` are required before an RDS DB instance can be created. See [Setting up for Amazon RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_SettingUp.html) for further instructions.

## Manual creation
Follow the [Creating an Amazon RDS DB instance](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_CreateDBInstance.html) guide for details on standing up an RDS DB instance

> Depending on your use-case, consideration should be given to the selected RDS Engine, this must be of a type supported by the Data Center product you wish to install:
* [Confluence supported databases](https://confluence.atlassian.com/doc/supported-platforms-207488198.html#SupportedPlatforms-Databases)
* [Jira supported databases](https://confluence.atlassian.com/adminjiraserver/supported-platforms-938846830.html#Supportedplatforms-Databases)
* [Bitbucket supported databases](https://confluence.atlassian.com/bitbucketserver/supported-platforms-776640981.html#Supportedplatforms-databasesDatabases)

> Having created a DB continue with provisioning the [pre-requisite infrastructure](../../PREREQUISITES.md)