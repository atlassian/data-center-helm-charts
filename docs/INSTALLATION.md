# Installation 

Use these instructions to install your Atlassian product using the Helm charts. Make sure you have followed the [Prerequisites guide](PREREQUISITES.md) before you proceed with the installation.

## 1. Add the Helm chart repository

Add the Helm chart repository to your local Helm installation:

```shell
helm repo add atlassian-data-center https://atlassian-labs.github.io/data-center-helm-charts
```
Update the repo:
```shell:
helm repo update
```

## 2. Obtain `values.yaml`

Obtain default product `values.yaml` from chart:
```shell
helm show values atlassian-data-center/<product> > values.yaml
```

## 3. Configure database
Using the `values.yaml` obtained in [step 2.](#Obtain-values.yaml), configure usage of the database provisioned as part of the [Prerequisites](PREREQUISITES.md). 

> Providing all the required DB values means database connectivity configuration during the product setup will be bypassed.

First, create a K8s secret to store the connectivity details of the database:
```shell
kubectl create secret generic <secret_name> --from-literal=username='<db_username>' --from-literal=password='<db_password>'
``` 

Using the K8s secret, update the `database` stanza within `values.yaml` appropriately. Refer to the commentary within the `values.yaml` for additional details on how to configure the remaining DB values:
```yaml
database:
  type: <db_type>
  url: <jdbc_url>
  driver: <engine_driver>
  credentials:
    secretName: <secret_name>
    usernameSecretKey: username
    passwordSecretKey: password
```
> For additional information on how the above values should be configured, refer to the [database connectivity guide](CONFIGURATION.md#Database-connectivity).
    
## 4. Configure Ingress
Using the `values.yaml` obtained in [step 2.](#Obtain-values.yaml), configure the Ingress controller provisioned as part of the [Prerequisites](PREREQUISITES.md). The values supplied here will be used to provision an Ingress resource for the controller. Refer to the associated commentary within the `values.yaml` for additional details on how to configure the Ingress resource:

```yaml
ingress:
  create: true #1. Setting true here will create an Ingress resource
  nginx: true #2. If using the ingress-nginx controller set this property to true
  maxBodySize: 250m
  host: <dns_host_name> #2. Hosts can be precise matches (for example “foo.bar.com”) or a wildcard (for example “*.foo.com”).
  path: "/"
  annotations: {}
  https: true
  tlsSecretName:
```
> Additional details on Ingress controllers are documented [here](CONFIGURATION.md#Ingress), and an example of how to set up a controller can be found [here](examples/ingress/CONTROLLERS.md).
    
## 5. Configure persistent storage
Using the `values.yaml` obtained in [step 2.](#Obtain-values.yaml), configure usage of the shared home provisioned as part of the [Prerequisites](PREREQUISITES.md).

```yaml
volumes:
  sharedHome:
    customVolume:
      persistentVolumeClaim:
        claimName: <pvc_name>
```

Although not required, local storage for the pods can also be configured at this stage. A `StorageClass` will need to be pre-provisioned to utilize this feature, see [here for an example](examples/storage/aws/LOCAL_STORAGE.md). Having created the `StorageClass` update `values.yaml` to make use of it: 

```yaml
volumes:
  localHome:
    persistentVolumeClaim:
      create: true
      storageClassName: "ebs-sc"
```

> For more details, please refer to the [Volumes section of the configuration guide](CONFIGURATION.md#Volumes).
    
> **NOTE:** Bitbucket needs a dedicated NFS server providing persistence for a shared home. Prior to installing the Helm chart, a suitable NFS shared storage solution must be provisioned. The exact details of this resource will be highly site-specific, but you can use this example as a guide: [Implementation of an NFS Server for Bitbucket](examples/storage/nfs/NFS.md)
    
## 6. Install your chosen product: 

```shell
helm install <release-name> atlassian-data-center/<product> --namespace <namespace> --version <chart-version> --values values.yaml
```

* `<release-name>` is the name of your deployment and is up to you, or you can use `--generate-name`.
* `<product>` can be jira, confluence, bitbucket or crowd.
* `<namespace>` is optional. You can use namespaces to organize clusters into virtual sub-clusters.
* `<chart-version>` is optional, and can be omitted if you just want to use the latest version of the chart.
* `values.yaml` is optional and contains your site-specific configuration information. If omitted, the chart config default will be used.
* Add `--wait` if you wish the installation command to block until all of the deployed Kubernetes resources are ready, but be aware that this may be waiting for several minutes if anything is mis-configured.

## 7. Test your deployed product 

Make sure the service pod/s are running, then test your deployed product:

```shell
helm test <release-name> --logs --namespace <namespace>
```

* This will run some basic smoke tests against the deployed release.
* If any of these tests fail, it is likely that the deployment was not successful. Please check the status of the deployed resources for any obvious errors that may have caused the failure.

## 8. Complete your product setup 

Using the service URL supplied by Helm post install, open your product in a web browser and complete the setup via the setup wizard. 

# Uninstall  
```shell
helm uninstall <release-name> atlassian-data-center/<product>
```

* `<release-name>` is the name you chose for your deployment
* `<product>` can be jira, confluence, bitbucket or crowd

***

* Continue to the [operation guide](OPERATION.md)
* Go back to the [prerequisites](PREREQUISITES.md) 
* Dive deeper into the [configuration](CONFIGURATION.md) options 
* Go back to [README.md](../README.md)