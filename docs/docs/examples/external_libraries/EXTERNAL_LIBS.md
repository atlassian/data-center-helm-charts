# External libraries and plugins

!!!warning "`.jar` files only"

    Whether loading external libraries, drivers or plugins, the approaches outlined here can only be used with `.jar` files. Plugin `obr` files can be extracted (unzipped) to access the included `.jar`

In some situations, you may want to load 3rd party plugins, drivers or libraries so that they are available to the product 
being installed.

An example of when this may be needed are for those products that do not ship with the appropriate `MySQL` and `Oracle` 
`JDBC` drivers.

There are 3 strategies for doing this, you can either:

*  [use the required prerequisite shared home volume](#shared-home-volume) 
*  [create a custom volume specifically for this purpose](#custom-volume) 
*  [provide a custom command for the `nfsPermissionFixer`](#custom-command)

Each approach will be discussed below.

!!!info "Approach"

    Which approach is used is totally up to you. For convenience you may want to just use [shared-home](../../userguide/PREREQUISITES.md#configure-a-shared-home-volume), or if you'd like to 
    keep things clean you may decide to mount these 3rd party libraries in a volume of their own. This approach would be 
    particularly useful when these libraries need to be shared with other Pod's in your cluster.

## Shared home volume
This approach consists of 3 high-level tasks:

1. Create sub-dir in `shared-home` volume
2. Copy libraries to sub-dir
3. Update `additionalLibraries` stanza in `values.yaml`

### 1. Create sub-dir
Add the Pod definition below to a file called `shared-home-browser.yaml` 

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: shared-home-browser
spec:
  containers:
    - name: browser
      image: debian:stable-slim
      volumeMounts:
        - mountPath: /shared-home
          name: shared-home
      command: [ "bash", "-c", "--" ]
      args: [ "while true; do sleep 30; done;" ]
  volumes:
    - name: shared-home
      persistentVolumeClaim:
        claimName: <shared-home-pvc-name>
```
Initialise the Pod in the same namespace in which the `shared-home` PVC was created
```bash
kubectl apply -f shared-home-browser.yaml
```
Once running execute the following command, it will create the sub-sir, `libraries`, under `/shared-home`
```bash
kubectl exec -it shared-home-browser -- bash -c "mkdir -p /shared-home/libraries"
```

### 2. Copy libraries to sub-dir
Now copy the files you require to the sub-dir by using the `kubectl cp` command
```bash
kubectl cp my_library.jar shared-home-browser:/shared-home/libraries
```

### 3. Update `values.yaml`
Update the stanza, `additionalLibraries`, in `values.yaml` accordingly:
```yaml
additionalLibraries:
  - volumeName: shared-home
    subDirectory: libraries
    fileName: my_library.jar
```
With this config these files (`my_library.jar`) will be injected into the container directory `<product-installation-directory>/lib`. For more info on how these files are injected into the appropriate product container location, see Jira's helper [jira.additionalLibraries](https://github.com/atlassian/data-center-helm-charts/blob/main/src/main/charts/jira/templates/_helpers.tpl#L180).  

## Custom volume
This approach is very similar to the [Shared home volume](#shared-home-volume) approach, only a custom volume is created and used as opposed `shared-home`. 

1. Create a new volume for storing 3rd party libraries
2. Create sub-dir for the new volume
3. Copy libraries to sub-dir
4. Update `additionalLibraries` stanza in `values.yaml`
5. Update `additionalVolumeMounts` stanza in `values.yaml`
6. Update `additional` stanza in `values.yaml`

!!!info "Steps"

    Because many of the steps for this approach are similar to the steps used for [Shared home volume](#shared-home-volume) only those that differ will be discussed.

### 1. Create new volume
Using the same approach taken for provisioning the [shared-home volume](../storage/aws/SHARED_STORAGE.md), create a new `EFS` with a corresponding `PV` and `PVC`.

!!!warning "ReadOnlyMany"

    Ensure that the PV and PVC are setup with `ReadOnlyMany` access

### 2. Update `values.yaml`
Assuming that the `PVC` representing the `EFS` is called `third-party-libraries`, update the `values.yaml` so that the `PVC` is added as an `additional` mount:
```yaml
volumes:
  additional:
    - name: third-party-libraries
      persistentVolumeClaim:
        claimName: third-party-libraries
```
Now add this as an `additionalVolumeMounts`
```yaml
additionalVolumeMounts:
  - volumeName: third-party-libraries
    mountPath: /libraries
```
Finally inject the desired libraries by defining them under `additionalLibraries`
```yaml
additionalLibraries:
  - volumeName: third-party-libraries
    subDirectory: database_drivers
    fileName: my_library.jar
```

## Custom command
This example is based on the GitHub issue discussed [here](https://github.com/atlassian/data-center-helm-charts/issues/239). The `nfsPermissionFixer` in the `values.yaml` is used for appropriately setting the permissions on the `shared-home` volume. The command it uses for this is already defined by default, however
it can also be supplied with a custom `command` for adding 3rd party libraries to `shared-home`. The example below shows how this approach can be used for adding the `JDBC` `MySQL` driver:

```yaml linenums="1"
nfsPermissionFixer:
  command: |
    if [[ ! -f /shared-home/drivers/mysql-driver.jar ]]; then
      mkdir -p /shared-home/drivers
      apk add dpkg
      wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java_8.0.26-1debian10_all.deb
      dpkg-deb -R mysql-connector-java_8.0.26-1debian10_all.deb /tmp/
      cp /tmp/usr/share/java/mysql-connector-java-8.0.26.jar  /shared-home/drivers/mysql-driver.jar
    fi
    chgrp 2003 /shared-home; chmod g+w /shared-home
```
!!!warning "shared-home permissions"

    If taking this approach ensure the last thing your custom command does is apply the relevant permissions to the `shared-home` mount, see line `10` in `yaml` 
    snippet above. 

    Each product chart has a `sharedHome.permissionFix.command` helper for doing this. look at Jira's helper [sharedHome.permissionFix.command](https://github.com/atlassian/data-center-helm-charts/blob/main/src/main/charts/jira/templates/_helpers.tpl#L102) 
    for more details on how these permissions are applied by default.

Remember to also update the `additionalLibraries` stanza accordingly:
```yaml
additionalLibraries: 
  - volumeName: shared-home
    subDirectory: drivers
    fileName: mysql-driver.jar
```