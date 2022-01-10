# Migration

If you already have an existing Data Center product deployment, you can migrate it to a Kubernetes cluster using the Data Center Helm charts. 

You will need to migrate your database and your shared home (including local home for Bamboo), then all you need to do is to follow the [Installation guide](INSTALLATION.md), using your migrated resources instead of provisioning new ones.

## Migrating your database

To migrate your database, you should point the Helm charts to the existing database or to a migrated version of the database. Do this by updating the `database` stanza in the `values.yaml` file as explained in the [Configure database step in the installation guide](INSTALLATION.md#3-configure-database).

## Migrating your shared home

Application nodes should have access to a shared directory in the same path. Examples of what the shared file system stores include plugins, shared caches, repositories, attachments, and avatars. Configure your shared home by updating the `sharedHome` stanza in the `values.yaml` file as explained in the [Configure persistent storage step in the installation guide](INSTALLATION.md#5-configure-persistent-storage).

## Migrating Bamboo server local home

Bamboo DC stores pertinent config data in local home, namely `bamboo.cfg.xml`. Care should be taken to include this data when migrating Bamboo DC deployments.

## Helpful links

* [Atlassian Data Center migration plan](https://confluence.atlassian.com/enterprise/atlassian-data-center-migration-plan-935363952.html){.external} - gives some guidance on overall process, organizational preparedness, estimated time frames, and app compatibility. 
* [Atlassian Data Center migration checklist](https://confluence.atlassian.com/enterprise/atlassian-data-center-migration-checklist-935383667.html){.external} - also provides useful tests and checks to perform throughout the moving process.
* Migrating to another database - describes how to migrate your data from your existing database to another database:
    * [Migrating Confluence to another database](https://confluence.atlassian.com/doc/migrating-to-another-database-148867.html){.external}
    * [Migrating Jira to another database](https://confluence.atlassian.com/adminjiraserver/switching-databases-938846867.html){.external} 
    * [Migrating Bamboo to another database](https://confluence.atlassian.com/bamboo/moving-your-bamboo-data-to-a-different-database-289277250.html){.external} 

!!!tip "Availability Zone proximity"
    For better performance consider co-locating your migrated database in the same Availability Zone (AZ) as your product nodes. Database-heavy operations, such as full re-index, become significantly faster when the database is collocated with the Data Center node in the same AZ. However we don't recommend this if you're running critical workloads.
