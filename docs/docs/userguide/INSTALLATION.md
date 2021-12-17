# Installation 

Follow these instructions to install your Atlassian product using the Helm charts. Before you proceed with the installation, make sure you have followed the [Prerequisites guide](PREREQUISITES.md).

## 1. Add the Helm chart repository

Add the Helm chart repository to your local Helm installation:

```shell
helm repo add atlassian-data-center \
 https://atlassian.github.io/data-center-helm-charts
```

Update the repository:

```shell
helm repo update
```

## 2. Obtain `values.yaml`

Obtain the default product `values.yaml` file from the chart:

```shell
helm show values atlassian-data-center/<product> > values.yaml
```

!!!warning "Bamboo deployments"

    If deploying Bamboo, be sure to read about the current limitations relating to [Bamboo deployments and values.yaml](../../troubleshooting/LIMITATIONS/#deployment)

## 3. Configure database
Using the `values.yaml` file obtained in [step 2](#2-obtain-valuesyaml), configure the usage of the database provisioned as part of the [prerequisites](PREREQUISITES.md). 

!!!tip "Automated setup steps"
    By providing all the required database values, you will bypass the database connectivity configuration during the product setup.

!!!info "Migration"
    If you are migrating an existing Data Center product to Kubernetes, use the values of your product's database. See [Migration guide](MIGRATION.md).

Create a Kubernetes secret to store the connectivity details of the database:

```shell
kubectl create secret generic <secret_name> --from-literal=username='<db_username>' --from-literal=password='<db_password>'
``` 

Using the Kubernetes secret, update the `database` stanza within `values.yaml` appropriately. Refer to the commentary within the `values.yaml` file for additional details on how to configure the remaining database values:

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

!!!info "Database connectivity"
    For additional information on how the above values should be configured, see the [Database connectivity section of the configuration guide](CONFIGURATION.md#database-connectivity).

    Read about [Kubernetes secrets](https://kubernetes.io/docs/concepts/configuration/secret/){.external}.
    
## 4. Configure Ingress
Using the `values.yaml` file obtained in [step 2](#2-obtain-valuesyaml), configure the [Ingress controller](https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/){.external} provisioned as part of the [Prerequisites](PREREQUISITES.md). The values you provide here will be used to provision an Ingress resource for the controller. Refer to the associated comments within the `values.yaml` file for additional details on how to configure the Ingress resource:

```yaml
ingress:
  create: true #1. Setting true here will create an Ingress resource
  nginx: true #2. If using the ingress-nginx controller set this property to true
  maxBodySize: 250m
  host: <dns_host_name> #2. Hosts can be precise matches (for example “foo.bar.com”) or a wildcard (for example “*.foo.com”).
  path: "/"
  annotations:
    cert-manager.io/issuer: <certificate_issuer>
  https: true
  tlsSecretName: <tls_certificate_name>
```

!!!info "Ingress configuration"
    For additional details on Ingress controllers see [the Ingress section of the configuration guide](CONFIGURATION.md#ingress). 

    See an example of [how to set up a controller](../examples/ingress/CONTROLLERS.md).
    
## 5. Configure persistent storage

Using the `values.yaml` file obtained in [step 2](#2-obtain-valuesyaml), configure the `shared-home` that was provisioned as part of the [Prerequisites](PREREQUISITES.md). See [shared home example](../examples/storage/aws/SHARED_STORAGE.md).

If you are [migrating an existing Data Center product to Kubernetes](MIGRATION.md), use the values of your product's shared home. 


```yaml
volumes:
  sharedHome:
    customVolume:
      persistentVolumeClaim:
        claimName: <pvc_name>
```

Each pod will also require its own `local-home` storage. This can be configured with a `StorageClass`, as can be seen in the [local home example](../examples/storage/aws/LOCAL_STORAGE.md). Having created the `StorageClass`, update `values.yaml` to make use of it: 

```yaml
volumes:
  localHome:
    persistentVolumeClaim:
      create: true
      storageClassName: <storage-class-name>
```
!!!info "Volume configuration"
    For more details, refer to the [Volumes section of the configuration guide](CONFIGURATION.md#volumes).
    
!!!tip "Bitbucket shared storage"
    Bitbucket needs a dedicated NFS server providing persistence for a shared home. Prior to installing the Helm chart, a suitable NFS shared storage solution must be provisioned. The exact details of this resource will be highly site-specific, but you can use this example as a guide: [Implementation of an NFS Server for Bitbucket](../examples/storage/nfs/NFS.md).
    
## 6. Configure clustering

By default, the Helm charts will not configure the products for Data Center clustering. You can enable clustering in the `values.yaml` file:

```yaml
  clustering:
    enabled: true
```

!!!warning "Bamboo clustering"
    Because of the limitations outlined under [Bamboo and clustering](../troubleshooting/LIMITATIONS.md#cluster-size) the `clustering` stanza is not available as a configurable property in the Bamboo `values.yaml`.

  
## 7. Configure license 

!!!info "Pre-configuring license"
    Pre-provisioning a license in this way is only applicable to `Confluence`, `Bitbucket` and `Bamboo` deployments. For `Jira` deployments a license can be supplied via the setup wizard post deployment.

You can configure the product license if you provide a `license` stanzas within the `values.yaml` obtained in [step 2](#2-obtain-valuesyaml). To do that, create a Kubernetes secret to hold the product license:

```shell
kubectl create secret generic <license_secret_name> --from-literal=license-key='<product_license_key>'
```

Update the `values.yaml` file with the secrets:

```yaml
license:
  secretName: <secret_name>
  secretKey: license-key
```
???tip "Sysadmin credentials for Bitbucket and Bamboo "

    `Bitbucket` and `Bamboo` are slightly different from the other products in that they can be completely configured during deployment, meaning no manual setup is required. To do this, you need to update the `sysadminCredentials` and also provide the `license` stanza from the previous step.

    Create a Kubernetes secret to hold the Bitbucket/Bamboo system administrator credentials:

    ```shell
    kubectl create secret generic <sysadmin_creds_secret_name> --from-literal=username='<sysadmin_username>' --from-literal=password='<sysadmin_password>' --from-literal=displayName='<sysadmin_display_name>' --from-literal=emailAddress='<sysadmin_email>'
    ```

    Update the `values.yaml` file with the secrets:

    ```yaml
    sysadminCredentials:
      secretName: <sysadmin_creds_secret_name>
      usernameSecretKey: username
      passwordSecretKey: password
      displayNameSecretKey: displayName
      emailAddressSecretKey: emailAddress
    ```

## 8. Install your chosen product

```shell
helm install <release-name> \
             atlassian-data-center/<product> \
             --namespace <namespace> \
             --version <chart-version> \
             --values values.yaml
```

!!!note "Values & flags"
* `<release-name>` the name of your deployment. You can also use `--generate-name`.
* `<product>` the product to install. Options include: 
  
    * `jira` 
    * `confluence`
    * `bitbucket`
    * `bamboo`
    * `bamboo-agent`
    * `crowd`
  
* `<namespace>` optional flag for categorizing installed resources.
* `<chart-version>` optional flag for defining the [chart version](https://artifacthub.io/packages/search?org=atlassian&sort=relevance&page=1){.external} to be used. If omitted, the latest version of the chart will be used.
* `values.yaml` optional flag for defining your site-specific configuration information. If omitted, the chart config default will be used.
* Add `--wait` if you wish the installation command to block until all of the deployed Kubernetes resources are ready, but be aware that this may wait for several minutes if anything is mis-configured.
    
!!!info "Elasticsearch for Bitbucket"
    We highly recommend you use an external Elasticsearch installation for Bitbucket. When you run more than one node you need to have a separate Elasticsearch cluster to enable code search. See [Bitbucket Elasticsearch recommendations](../examples/bitbucket/BITBUCKET_ELASTICSEARCH.md).    
        

## 9. Test your deployed product 

Make sure the service pod/s are running, then test your deployed product:

```shell
helm test <release-name> --logs --namespace <namespace>
```

* This will run some basic smoke tests against the deployed release.
* If any of these tests fail, it is likely that the deployment was not successful. Check the status of the deployed resources for any obvious errors that may have caused the failure.

## 10. Complete product setup 

Using the service URL provided by Helm post install, open your product in a web browser and complete the setup via the setup wizard. 

## 11. Additional deployments

Bamboo agents and Bitbucket mirrors can also be deployed via their dedicated charts:

=== "Bamboo agent"
    !!!info "Bamboo agent installation"
        [Instructions for deploying Bamboo agents](../examples/bamboo/REMOTE_AGENTS.md)

=== "Bitbucket mirror"
    !!!info "Bitbucket mirror installation"
        [Instructions for deploying Bitbucket mirror's](../examples/bitbucket/BITBUCKET_MIRRORS.md)

# Uninstall
The deployment and all of its associated resources can be un-installed with the following command:
```shell
helm uninstall <release-name> atlassian-data-center/<product>
```
