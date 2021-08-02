# Confluence rolling upgrade
Let's say we have Confluence version `7.4.0` deployed to our Kubernetes cluster, and we want to upgrade it to version
`7.4.1`, which we'll call the *target version*. You can substitute the target version for the one you need, as long as
it's newer than the current one.

## 1. Find tag of the target image

Go to [atlassian/confluence-server](https://hub.docker.com/r/atlassian/confluence-server/tags){.external}
Docker Hub page to pick a tag that matches your target version.

In the example we're running Confluence using the `7.4.0-jdk11` tag, and we'll be upgrading to `7.4.1-jdk11` - our *target*.

## 2. Put Confluence into upgrade mode

From the admin page click on *Rolling Upgrade* and set the Confluence in Upgrade mode:

  ![upgrade-mode](../../assets/images/confluence-upgrade-1.png)

## 3. Run the upgrade using Helm

Run Helm *upgrade* command with your release name (`<release-name>`) and the target image from a previous step
(`<target-tag>`). For more details, consult the [Helm documentation](https://helm.sh/docs/).

 ```shell
 $ helm upgrade <release-name>  atlassian-data-center/confluence --wait --reuse-values --set image.tag=<target-tag>
 ```

If you used `kubectl scale` after installing the Helm chart, you'll need to add `--set
replicaCount=<number-of-confluence-nodes>` to the command. Otherwise, the deployment will be scaled back to the original
number which, most likely, is one node.

## 4. Wait for the upgrade to finish
The pods will be re-created with the updated version, one at a time.

![upgrade-mode](../../assets/images/confluence-upgrade-2.png)

## 5. Finalize the upgrade
After all pods activated with the new version, finalize the upgrade:

![upgrade-mode](../../assets/images/confluence-upgrade-3.png)
