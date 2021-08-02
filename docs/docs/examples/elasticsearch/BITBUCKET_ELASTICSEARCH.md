# Bitbucket Elasticsearch recommendations
While Bitbucket has its own internal Elasticsearch instance, we highly recommend you use an external Elasticsearch installation, either within the Kubernetes cluster or, if available, an instance managed by your hosting provider.

## Installing and configuring Elasticsearch in your Kubernetes cluster
### Installing Elasticsearch into your Kubernetes cluster
Choose a version of Elasticsearch that is supported by the [version of Bitbucket you are installing](https://confluence.atlassian.com/bitbucketserver/supported-platforms-776640981.html#Supportedplatforms-additional-toolsAdditionaltools){.external}. For Bitbucket 7.14 the latest supported Elasticsearch version is 7.9.3, so we will target that.

There are official [Helm charts for Elasticsearch 7.9.3](https://artifacthub.io/packages/helm/elastic/elasticsearch/7.9.3){.external}. Following the documentation there add the Elasticsearch Helm charts repository, then install it:

`helm repo add elastic https://helm.elastic.co`

`helm install elasticsearch --version 7.9.3 elastic/elasticsearch`

### Configuring your Bitbucket deployment

To enable using the installed Elasticsearch service you need to to configure the service URL under `bitbucket:` in the `values.yaml` file:
```yaml
bitbucket:
  elasticSearch:
    baseUrl: http://elasticsearch:9200
```
This will also have the effect of disabling Bitbucket’s internal Elasticsearch instance.

If you have configured authentication in the deployed Elasticsearch you will also need to provide the details in a Kubernetes secret and configure that in the `values.yaml` file:
```yaml
    credentials:
      secretName: <my-elasticsearch-secret>
      usernameSecreyKey: username
      passwordSecretKey: password
```
> Read about [Kubernetes secrets](https://kubernetes.io/docs/concepts/configuration/secret/){.external}.



## Configuring Amazon Elasticsearch Service with Bitbucket on Kubernetes

### Creating an Amazon Elasticsearch Service domain with a master user

The Elasticsearch instance (“domain”) can be created via the AWS CLI or the web console; for this example we will use the web console and a master user:

1. In the EKS console navigate to **Your Cluster → Networking** and note the VPC ID.
2. In the Elasticsearch console create a new domain:
   1. Select a production deployment.
   2. Select Elasticsearch version 7.9.
3. In the next screen configure the AZs and nodes as appropriate for your expected workload.
4. On the **Access and security** page:
   1. Select the same VPC as the EKS cluster, as noted in step 1.
   2. Select appropriate subnets for each AZ; private subnets are fine.
   3. Select appropriate security groups that will grant node/pod access.
   4. Tick **Fine–grained access control**:
     * Select **Create master user** and add a username and a strong password.
5. Configure tags, etc. as appropriate for your organisation.

Once the Elasticsearch domain has finished creating, make a note of the **VPC Endpoint**, which will be an HTTPS URL.

### Configuring your Bitbucket deployment

To use the managed Elasticsearch service, first create a Kubernetes secret using the username and password from step 4 above. Then configure the service URL under `bitbucket:` in the `values.yaml` file, substituting the values below from the above steps where appropriate:
```yaml
bitbucket:
  elasticSearch:
    baseUrl: <VPC Endpoint>
    credentials:
      secretName: <my-elasticsearch-secret>
      usernameSecreyKey: username
      passwordSecretKey: password
```
> Read about [Kubernetes secrets](https://kubernetes.io/docs/concepts/configuration/secret/){.external}.
