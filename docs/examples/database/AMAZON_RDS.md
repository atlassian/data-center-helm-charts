# Creating an RDS DB instance
Instructions for creating an Amazon [RDS DB instance](https://aws.amazon.com/rds/).

## Prerequisites
* An `AWS account`, `IAM user`, `VPC` (or [default VPC](https://docs.aws.amazon.com/vpc/latest/userguide/default-vpc.html)) and `security group` are required before an RDS DB instance can be created. See [Setting up for Amazon RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_SettingUp.html) for further instructions.

## Manual creation
Database creation comprises of 2 steps:

1. Initialize the database server
2. Create a database user and corresponding database

###1. Initialize database server
Follow the [Creating an Amazon RDS DB instance](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_CreateDBInstance.html) guide for details on standing up an RDS DB server.

###2. Create database
For details on creating the database user and database itself follow the appropriate guide below:
 * [Jira](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-a-database-938846850.html)
 * [Confluence](https://confluence.atlassian.com/doc/database-configuration-159764.html#DatabaseConfiguration-Databasesetupsetup)
 * [Bitbucket](https://confluence.atlassian.com/bitbucketserver/connect-bitbucket-to-an-external-database-776640378.html)
 * [Crowd](https://confluence.atlassian.com/crowd/connecting-crowd-to-a-database-4030904.html)

> Having created a DB continue with provisioning the [pre-requisite infrastructure](../../PREREQUISITES.md).