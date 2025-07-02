!!!info "Helm chart version"
    OpenSearch sub-chart is supported in Bitbucket Helm chart version 1.20 onwards.

## When to use which OpenSearch configuration

There are two different ways to configure OpenSearch with Bitbucket on Kubernetes:

### 1. Deploy OpenSearch Helm chart (self-managed)

Use the `opensearch` section in your Helm values file when you want to deploy and manage your own OpenSearch cluster within the Kubernetes cluster. This approach:

* Deploys the [OpenSearch Helm chart](https://github.com/opensearch-project/helm-charts/tree/main/charts/opensearch){.external} alongside Bitbucket
* Automatically configures Bitbucket to connect to the deployed OpenSearch cluster
* Requires managing the OpenSearch cluster lifecycle (updates, backups, scaling, etc.)

### 2. Use AWS OpenSearch Service (managed service)

Use the `bitbucket.additionalEnvironmentVariables` section when you want to connect to an external AWS OpenSearch Service (managed service). This approach:

* Connects to an existing AWS OpenSearch Service domain
* Leverages AWS's managed service benefits (automatic backups, updates, scaling, etc.)
* Requires manual configuration through environment variables

!!!warning "Important: Choose the right configuration method"
    **Do NOT use the `opensearch` section if you want to connect to AWS OpenSearch Service.** The `opensearch` section is designed to deploy a self-managed OpenSearch cluster within your Kubernetes cluster, not to connect to external services.

## Deploy OpenSearch Helm chart with Bitbucket

!!!warning "Support disclaimer"
    Atlassian does not officially support OpenSearch Helm chart that can be installed with the Bitbucket Helm release. Should you encounter any issues with the deployment, maintenance and upgrades, reach out to the [vendor](https://github.com/opensearch-project/helm-charts/tree/main/charts/opensearch){.external}.
    Moreover, if you intend to deploy OpenSearch to a critical Kubernetes environment, make sure you follow all the best practices, i.e. deploy a multi node cluster, use taints and tolerations, affinity rules, sufficient resources requests, have DR and backup strategies etc.

To deploy OpenSearch Helm chart and automatically configure Bitbucket to use it as a search platform, set the following in your Helm values file:

```yaml
opensearch:
  install: true
```
This will:

* auto-generate the initial OpenSearch admin password and create a Kubernetes secret with `OPENSEARCH_INITIAL_ADMIN_PASSWORD` key
* deploy [OpenSearch Helm chart](https://github.com/opensearch-project/helm-charts/tree/main/charts/opensearch){.external} to the target namespace with the default settings: single node, no SSL, 1Gi memory/1 vCPU resources requests, 10Gi storage request
* set `PLUGIN_SEARCH_CONFIG_BASEURL` to `http://opensearch-cluster-master:9200` unless overridden in `opensearch.baseUrl`
* set `PLUGIN_SEARCH_CONFIG_USERNAME` to `admin` (this is the initial admin user created when the OpenSearch cluster starts for the very first time)
* set `PLUGIN_SEARCH_CONFIG_PASSWORD` to a randomly generated password saved to `opensearch-initial-password` secret

## Create dedicated OpenSearch user

When the Helm chart is installed with the default values, OpenSearch admin user is created with an auto-generated password, and Bitbucket is configured to use these credentials to connect to OpenSearch. If you want to have a more fine-grained control over internal users, you may pre-create a secret with a list of users, their credentials and roles. See [internal_users.yml](https://opensearch.org/docs/latest/security/configuration/yaml/#internal_usersyml){.external} for more details.

### Hash passwords

Passwords in `internal_users.yml` must be hashed. You can use `hash.sh` script bundled with Bitbucket to hash your passwords, for example:

```shell
docker run atlassian/bitbucket:latest /bin/bash -c "chmod +x /opt/atlassian/bitbucket/opensearch/plugins/opensearch-security/tools/hash.sh && /opt/atlassian/bitbucket/opensearch/plugins/opensearch-security/tools/hash.sh -p mySecureAdminPassword123"

$2y$12$bcUjaXcfutyYwkjp6r/RdePrywC3BmQLKvN77XuuR0PJs0qjBooSv
```
If Bitbucket is already up and running in your Kubernetes cluster, you can run exec into the Bitbucket container to hash a password:

```shell
kubectl exec -ti bitbucket-0 -n atlassian -- /bin/bash -c  "chmod +x /opt/atlassian/bitbucket/opensearch/plugins/opensearch-security/tools/hash.sh && /opt/atlassian/bitbucket/opensearch/plugins/opensearch-security/tools/hash.sh -p mySecureBitbucketPassword123"

$2y$12$x910YF09tcfhNs009vOzWOMy9fswhpsuV5/AiiPwbY4rp5BXKv2tu
```

### Create internal_users.yml file

This is the minimal `internal_users.yml` file with an admin and a dedicated bitbucket user. See [internal_users.yml](https://opensearch.org/docs/latest/security/configuration/yaml/#internal_usersyml){.external} to get more details about users, roles and role mappings in OpenSearch.

```yaml
_meta:
  type: "internalusers"
  config_version: 2

admin:
  hash: "$2y$12$bcUjaXcfutyYwkjp6r/RdePrywC3BmQLKvN77XuuR0PJs0qjBooSi"
  reserved: true
  backend_roles:
  - "admin"
  description: "Demo admin user"

bitbucket:
  hash: "$2y$12$x910YF09tcfhNs009vOzWOMy9fswhpsuV5/AiiPwbY4rp5BXKv2tu"
  reserved: true
  backend_roles:
    - "admin"
  description: "Bitbucket admin user"
```

### Create Kubernetes secret

Create a Kubernetes secret with 3 keys in its data:

!!!warning "internal_users.yml"
    Make sure there are no typos in `internal_users.yml` filename, otherwise OpenSearch pods can't be created due to a missing key in the secret.

```shell
kubectl create secret generic opensearch-internal-users \
      --from-literal=username=bitbucket \
      --from-literal=password="mySecureBitbucketPassword123" \
      --from-file=internal_users.yml=/path/to/internal_users.yml \
      -n atlassian
```

### Update Helm values

Now that the secret has been created, update your Helm values to point OpenSearch and Bitbucket to `opensearch-internal-users` secret:

```yaml
opensearch:
  install: true
  credentials:
    secretName: opensearch-internal-users
    usernameSecretKey: username
    passwordSecretKey: password
  securityConfig:
    internalUsersSecret: opensearch-internal-users
```

If necessary, you can create 2 separate secrets - one for the `internal_users.yml` only, and one with the actual non-hashed OpenSearch credentials that Bitbucket will use.

## Override OpenSearch Helm chart values

You can configure your OpenSearch cluster and the deployment options by overriding any values that the [Helm chart](https://github.com/opensearch-project/helm-charts/blob/main/charts/opensearch/values.yaml){.external} exposes. OpenSearch values must be nested under `opensearch` stanza in your Helm values file, for example:

```yaml
opensearch:
  singleNode: false
  replicas: 5
  config:
    opensearch.yml: |
      cluster.name: custom-cluster
```

## Enable SSL with Custom Certificates 

By default, OpenSearch starts with the SSL http plugin disabled, meaning the cluster is accessible via HTTP at http://opensearch-cluster-master:9200. The OpenSearch service is not exposed through a LoadBalancer or Ingress unless the default configurations are explicitly overridden. Bitbucket communicates with the OpenSearch cluster using the service name within the internal Kubernetes network. This setup uses the Kubernetes DNS to resolve the service name to the appropriate cluster IP address.

To enable SSL in OpenSearch and start the service on a secure port, you need to:
* Enable SSL HTTPS in Helm values.
* Create Kubernetes secrets with `ca`, `certificate` and `key`, and pass them to OpenSearch.
* Add `ca.crt` to Java trust store in Bitbucket containers if the custom certificate is not signed by a public authority.

Below is an **example** (not the recommended way) of how to generate a CA certificate, a server certificate, and a corresponding private key for securing communications with OpenSearch:

```shell
openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 -subj "/C=US/ST=CA/L=LA/O=MyCompany Name/CN=opensearch-cluster-master" -keyout ca.key -out ca.crt
openssl req -new -newkey rsa:2048 -nodes -keyout opensearch-cluster-master.key -subj "/C=US/ST=CA/L=LA/O=MyCompany Name/CN=opensearch-cluster-master" -out opensearch-cluster-master.csr
openssl x509 -req -days 365 -in opensearch-cluster-master.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out opensearch-cluster-master.crt
```

It is important to generate the certificate for `opensearch-cluster-master` CN because this is the actual OpenSearch service/host name in the target namespace.

Once done, create 3 Kubernetes secrets:

```shell
kubectl create secret generic opensearch-ssl-ca -n atlassian --from-file=root-ca.pem=ca.crt
kubectl create secret generic opensearch-ssl-key -n atlassian --from-file=esnode-key.pem=opensearch-cluster-master.key
kubectl create secret generic opensearch-ssl-cert -n atlassian --from-file=esnode.pem=opensearch-cluster-master.crt
```

Enable ssl http plugin, override the default ca, certificate and key, as well as provide additional volume mounts in Bitbucket Helm values file:

```yaml
opensearch:
  extraEnvs:
    - name: plugins.security.ssl.http.enabled
      value: "true"
    - name: plugins.security.ssl.transport.pemkey_filepath
      value: "esnode-key.pem"
    - name: plugins.security.ssl.transport.pemcert_filepath
      value: "esnode.pem"
    - name: plugins.security.ssl.transport.pemtrustedcas_filepath
      value: "root-ca.pem"
  secretMounts:
  - name: opensearch-ssl-cert
    secretName: opensearch-ssl-cert
    path: /usr/share/opensearch/config/esnode.pem
    subPath: esnode.pem
  - name: opensearch-ssl-key
    secretName: opensearch-ssl-key
    path: /usr/share/opensearch/config/esnode-key.pem
    subPath: esnode-key.pem
  - name: opensearch-ssl-ca
    secretName: opensearch-ssl-ca
    path: /usr/share/opensearch/config/root-ca.pem
    subPath: root-ca.pem
```
When using a self-signed certificate or a certificate not issued/signed by a recognized public certificate authority (CA), it is essential to add the CA certificate to the Java trust store in all Bitbucket containers:

```yaml
bitbucket:
  additionalCertificates:
    secretName: opensearch-ssl-ca
```

Additionally, you need to update the protocol specified in the baseUrl to HTTPS:

```yaml
bitbucket:
  additionalCertificates:
    secretName: opensearch-ssl-ca
opensearch:
  baseUrl: https://opensearch-cluster-master:9200
```

## Enable Ingress

To communicate with the OpenSearch cluster through a fully qualified domain name rather than via Kubernetes internal DNS name, you can enable an Ingress in the OpenSearch Helm chart. Below is an example of how to configure Ingress and update base URL in the Helm values file:


```yaml
opensearch:
  baseUrl: https://myopensearch.com
  ingress:
    install: true
```
Important considerations:

* Ensure that the `baseUrl` is set to use HTTPS protocol, which encrypts the data exchanged with the OpenSearch cluster
* Ensure that the Ingress hostname can be resolved from within the Kubernetes cluster network
* If the certificate is not signed by a public authority, you will need to add the certificate to Java trust store in Bitbucket containers by defining a secret name in `bitbucket.additionalCertificates.secretName`

## Using AWS OpenSearch Service

If you're running Bitbucket on Amazon EKS and want to use AWS OpenSearch Service (managed service) instead of deploying your own OpenSearch cluster, you need to configure Bitbucket using the `bitbucket.additionalEnvironmentVariables` section.

!!!info "AWS OpenSearch Service benefits"
    AWS OpenSearch Service provides a fully managed OpenSearch experience with automatic backups, updates, scaling, security, and monitoring. This eliminates the operational overhead of managing your own OpenSearch cluster.

### Prerequisites

Before configuring Bitbucket to use AWS OpenSearch Service:

1. Create an AWS OpenSearch Service domain in the same region as your EKS cluster
2. Configure the domain's access policy to allow access from your EKS cluster
3. Note the domain endpoint URL (e.g., `https://search-my-domain-abcdefghijklmnop.us-east-1.es.amazonaws.com`)
4. Have the username and password for your OpenSearch domain (if using master user authentication)

### Configuration

To configure Bitbucket to use AWS OpenSearch Service, add the following to your Helm values file:

```yaml
bitbucket:
  clustering:
    enabled: true  # Required for OpenSearch integration
  additionalEnvironmentVariables:
    - name: SEARCH_ENABLED
      value: "false"  # Disable internal search
    - name: PLUGIN_SEARCH_CONFIG_BASEURL
      value: "https://search-my-domain-abcdefghijklmnop.us-east-1.es.amazonaws.com"
    - name: PLUGIN_SEARCH_CONFIG_USERNAME
      value: "master-username"  # Your OpenSearch master username
    - name: PLUGIN_SEARCH_CONFIG_PASSWORD
      value: "master-password"  # Your OpenSearch master password
```

### Using Kubernetes Secrets for Credentials

For better security, store your AWS OpenSearch credentials in a Kubernetes secret instead of hardcoding them in the values file:

```shell
kubectl create secret generic aws-opensearch-credentials \
  --from-literal=username="master-username" \
  --from-literal=password="master-password" \
  -n atlassian
```

Then reference the secret in your Helm values:

```yaml
bitbucket:
  clustering:
    enabled: true
  additionalEnvironmentVariables:
    - name: SEARCH_ENABLED
      value: "false"
    - name: PLUGIN_SEARCH_CONFIG_BASEURL
      value: "https://search-my-domain-abcdefghijklmnop.us-east-1.es.amazonaws.com"
    - name: PLUGIN_SEARCH_CONFIG_USERNAME
      valueFrom:
        secretKeyRef:
          name: aws-opensearch-credentials
          key: username
    - name: PLUGIN_SEARCH_CONFIG_PASSWORD
      valueFrom:
        secretKeyRef:
          name: aws-opensearch-credentials
          key: password
```

### AWS IAM Authentication

If your AWS OpenSearch domain uses IAM authentication instead of master user authentication, you can configure Bitbucket to use IAM roles:

1. Create an IAM policy with the necessary OpenSearch permissions
2. Attach the policy to your EKS node group role or use IAM Roles for Service Accounts (IRSA)
3. Configure the environment variables:

```yaml
bitbucket:
  clustering:
    enabled: true
  additionalEnvironmentVariables:
    - name: SEARCH_ENABLED
      value: "false"
    - name: PLUGIN_SEARCH_CONFIG_BASEURL
      value: "https://search-my-domain-abcdefghijklmnop.us-east-1.es.amazonaws.com"
    # When using IAM authentication, username and password are not required
    # AWS SDK will use the pod's IAM role for authentication
```

### Important Notes

* **Do not use both configurations**: If you're using AWS OpenSearch Service with `additionalEnvironmentVariables`, ensure that `opensearch.install` is set to `false` (which is the default) and do not configure the `opensearch` section
* **Clustering must be enabled**: OpenSearch integration requires `bitbucket.clustering.enabled: true`
* **HTTPS is recommended**: Always use HTTPS endpoints for AWS OpenSearch Service to ensure secure communication
* **Region considerations**: Ensure your AWS OpenSearch domain is in the same region as your EKS cluster for optimal performance and reduced latency

### Troubleshooting AWS OpenSearch Service

If you encounter issues connecting to AWS OpenSearch Service:

1. **Check the domain endpoint**: Verify the URL is correct and accessible from your EKS cluster
2. **Verify access policies**: Ensure your EKS cluster has proper access to the OpenSearch domain
3. **Test connectivity**: Use `kubectl exec` to test connectivity from a Bitbucket pod:
   ```shell
   kubectl exec -ti bitbucket-0 -n atlassian -- curl -u username:password https://your-opensearch-domain/_cluster/health
   ```
4. **Check CloudWatch logs**: Review AWS OpenSearch Service logs in CloudWatch for authentication or authorization errors

## Troubleshooting

### Authentication

If you run into auth issues (401 response from OpenSearch), check the following:

* exec into the Bitbucket container and run:
  ```shell
  curl -v -u ${PLUGIN_SEARCH_CONFIG_USERNAME}:${PLUGIN_SEARCH_CONFIG_PASSWORD} http://opensearch-cluster-master:9200/_cat/indices?v
  ```
  If you get 401, then the user or password passed to the Bitbucket container does not match user or password defined in the `internal_users.yml`

* you can find out where the actual values of `PLUGIN_SEARCH_CONFIG_USERNAME` and `PLUGIN_SEARCH_CONFIG_PASSWORD` environment variables come from by running:

    ```shell
    kubectl get pod bitbucket-0 -n atlassian -o jsonpath="{.spec.containers[0].env[?(@.name=='PLUGIN_SEARCH_CONFIG_USERNAME')]}"
    kubectl get pod bitbucket-0 -n atlassian -o jsonpath="{.spec.containers[0].env[?(@.name=='PLUGIN_SEARCH_CONFIG_PASSWORD')]}"
    ```
* Make sure that hashed password of the bitbucket user in the `internal_users.yml` file corresponds to the plain text password stored in the Kubernetes secret.

### Networking

The default `opensearch-cluster-master` hostname must be resolved to a Kubernetes service cluster IP, and the request is then forwarded directly to the pod endpoint.
If you see connection refused/timeout errors, make sure all OpenSearch pods are in a Ready state and the corresponding endpoints have been created:

```shell
kubectl describe pod -n atlassian -l=app.kubernetes.io/component=opensearch-cluster-master
```

```shell
kubectl get endpoints -n atlassian

NAME                                 ENDPOINTS                                         AGE
bitbucket                            10.0.2.31:7999,10.0.2.31:7990,10.0.2.31:5701      113m
opensearch-cluster-master            10.0.1.170:9200,10.0.1.170:9300                   113m
opensearch-cluster-master-headless   10.0.1.170:9600,10.0.1.170:9200,10.0.1.170:9300   113m
```
