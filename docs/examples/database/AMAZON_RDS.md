# Creating an RDS database instance
This example provides instructions for creating an Amazon [RDS DB instance](https://aws.amazon.com/rds/).

## Prerequisites
* An `AWS account`, `IAM user`, `VPC` (or [default VPC](https://docs.aws.amazon.com/vpc/latest/userguide/default-vpc.html)) and `security group` are required before an RDS DB instance can be created. See [Setting up for Amazon RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_SettingUp.html) for further instructions.

## Database creation
There are two steps for creating the database:

### 1. Initialize database server
For details on standing up an RDS DB server follow the guide: [Creating an Amazon RDS DB instance](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_CreateDBInstance.html). 
### 2. Create database
For details on creating the database user and database itself follow the appropriate guide below:
 * [Jira](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-a-database-938846850.html)
 * [Confluence](https://confluence.atlassian.com/doc/database-configuration-159764.html#DatabaseConfiguration-Databasesetupsetup)
 * [Bitbucket](https://confluence.atlassian.com/bitbucketserver/connect-bitbucket-to-an-external-database-776640378.html)
 * [Crowd](https://confluence.atlassian.com/crowd/connecting-crowd-to-a-database-4030904.html)

> Once you create a database continue with provisioning the [prerequisite infrastructure](../../PREREQUISITES.md).

***
* Go back to the [prerequisites](../../PREREQUISITES.md)
* Go back to the [examples](../EXAMPLES.md)
* Go back to [README.md](../../../README.md)
