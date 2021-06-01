## Jira Rolling Upgrade
Suppose we have a Jira cluster with 3 nodes, all running `Jira xx.xx.0-jdk11`, and an upgrade to
`Jira xx.xx.1-jdk11` is planned. Use these steps to complete the upgrade process:

1. Update the `version` and `appVersion` in the Helm chart (`src/main/charts/jira/Chart.yaml`):
 ```yaml
 version: 0.1.1
 appVersion: xx.xx.1-jdk11
 ```

2. Create a new version of the Helm package:
 ```shell script
 $ helm package src/main/charts/jira --destination target/helm
 This will create target/helm/jira-0.1.1.tgz
 ```

3. Go to **Administration > Applications > Jira upgrades** and click **Put Jira into upgrade mode**.

  ![upgrade-mode](./images/jira-upgrade-1.png)

4. Run helm upgrade command with the desired number of nodes after upgrade (replicaCount):
 ```shell script
 $ helm upgrade -n dcd --wait <release name> --set replicaCount=3 target/helm/jira-0.1.1.tgz --reuse-values
 ```

5. Upgrade will start by terminating one pod and creating a new pod with an updated version.

  ![upgrade-mode](./images/jira-upgrade-2.png)

6. After the new pod is up and running, the next pod will be upgraded until all pods are upgraded to the new version.

7. After all pods are active with the new version, click **Run upgrade tasks** to finalize the upgrade:

  ![upgrade-mode](./images/jira-upgrade-3.png)
