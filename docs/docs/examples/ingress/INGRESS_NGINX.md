# NGINX Ingress Controller - with TLS termination
[NGINX ingress controller](https://kubernetes.github.io/ingress-nginx/){.external} with automatic TLS certificate management using [cert-manager](https://cert-manager.io/docs/){.external} and certificates from [Let's Encrypt](https://letsencrypt.org/){.external}.

> **NOTE:** These instructions are for reference purposes only. They should be used for development and testing purposes only! Official instructions for deploying and configuring the controller can be found [here](https://kubernetes.github.io/ingress-nginx/deploy/){.external}.

These instructions are composed of 3 high-level parts:

1. Controller installation and configuration
2. Certificate manager installation and configuration
3. Ingress resource configuration

## Controller installation and configuration
We recommend installing the controller using its official [Helm Charts](https://github.com/kubernetes/ingress-nginx/tree/master/charts/ingress-nginx){.external}. You can also use the instructions below.

### 1. Add controller repo
Add the `ingress-nginx` Helm repo:
```shell
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
```
Update the repo:
```shell
helm repo update
```

### 2. Install controller
Create a new namespace for the Ingress controller:
```shell
kubectl create namespace ingress
```
Install the controller using Helm:
```shell
helm install ingress-nginx ingress-nginx/ingress-nginx --namespace ingress
```

### 3. DNS setup
Manually provision a new DNS record via your cloud provider or using [external-dns](https://github.com/kubernetes-sigs/external-dns){.external}.

Once created, associate the DNS record with the auto provisioned Load Balancer that was created in [Step 2. above](#2-install-controller). To do this first identify the name of the auto provisioned LB, this can be done by examining the deployed ingress services i.e.
```shell
kubectl get service | grep ingress-nginx    
```
the output of this command should look something like...
```shell
ingress-nginx-controller             LoadBalancer   10.100.22.16    b834z142d8118406795a34df35e10b17-38927090.eu-west-1.elb.amazonaws.com   80:32615/TCP,443:31787/TCP   76m
ingress-nginx-controller-admission   ClusterIP      10.100.5.36     <none>                                                                  443/TCP                      76m
```
Take note of the `LoadBalancer` and using it as a value update the DNS record so that traffic is routed to it.

> **NOTE:** It can take a few minutes for the DNS to resolve these changes.

## Certificate manager installation and configuration
K8s certificate management is handled using [cert-manager](https://cert-manager.io/){.external}.

### 1. Install cert-manager
Add the cert manager repo
```shell
helm repo add jetstack https://charts.jetstack.io
```

Update repos
```shell
helm repo update
```

Install the cert-manager using Helm
```shell
helm install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.3.1 \
  --set installCRDs=true
```

Confirm the cert-manager is appropriately installed:
```shell
kubectl get pods --namespace cert-manager
```

### 2. Create certificate issuer
Using the yaml spec below create and apply the certificate `Issuer` resource
> Ensure that the certificate issuer is installed in the same namespace that the Atlassian product will be deployed to.

```yaml
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: letsencrypt-prod
  namespace: <product_deployment_namespace>
spec:
  acme:
    # The ACME server URL
    server: https://acme-v02.api.letsencrypt.org/directory
    # Email address used for ACME registration
    email: <user_email>
    # Name of a secret used to store the ACME account private key
    privateKeySecretRef:
      name: letsencrypt-prod
    # Enable the HTTP-01 challenge provider
    solvers:
      - http01:
          ingress:
            class: nginx
```
Install the `Issuer` resource
```shell
kubectl apply -f issuer.yaml
```

## Ingress resource configuration
Now that the Ingress controller and certificate manager are setup the Ingress resource can be configured accordingly by updating the `values.yaml`.

### 1. Ingress resource config
For TLS cert auto-provisioning and TLS termination update the `ingress` stanza within the products `values.yaml`:
```yaml
ingress:
  create: true
  nginx: true
  maxBodySize: 250m
  host: <dns_record>
  path: "/"
  annotations:
    cert-manager.io/issuer: "letsencrypt-prod" # Using https://letsencrypt.org/
  https: true
  tlsSecretName: tls-certificate
```

## Bitbucket SSH configuration
> **NOTE:** Bitbucket requires additional Ingress config to allow for `SSH` access. See [NGINX Ingress controller config for SSH connections](../ssh/SSH_BITBUCKET.md) for details.

> Having created the Ingress controller continue with provisioning the [prerequisite infrastructure](../../installation/PREREQUISITES.md).
