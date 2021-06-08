# NGINX Ingress Controller - With TLS termination
> **NOTE:** These instructions are for reference purposes only. They should be used for development and testing purposes only!

Official instructions for deploying and configuring the controller can be found [here](https://kubernetes.github.io/ingress-nginx/deploy/).

These instructions are composed of 2 high-level parts:

1. Controller installation and configuration
2. Certificate issuer installation and configuration

## Controller installation and configuration
We recommend installing the controller using its official [Helm Charts](https://github.com/kubernetes/ingress-nginx/tree/master/charts/ingress-nginx). You can also use the instructions below.

### 1. Add controller repo
Add the `ingress-nginx` Helm repo:
```shell
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
```

### 3. Install controller
Install the controller using Helm:
```shell
helm install <release name> ingress-nginx/ingress-nginx
```

### 4. DNS setup
Manually provision a new DNS record (as described) via your cloud provider or using [external-dns](https://github.com/kubernetes-sigs/external-dns).

Once created associate the DNS record with the auto provisioned Load Balancer that was created in [Step 3. above](#3.-Install). To do this first identify the name of the auto provisioned LB, this can be done by examining the deployed ingress services i.e.
```shell
kubectl get service | grep ingress-nginx    
```
the output of this command should look something like...
```shell
NAME                                 TYPE           CLUSTER-IP      EXTERNAL-IP
ingress-nginx-controller             LoadBalancer   10.100.22.16    b834z142d8118406795a34df35e10b17-38927090.eu-west-1.elb.amazonaws.com   80:32615/TCP,443:31787/TCP   76m
ingress-nginx-controller-admission   ClusterIP      10.100.5.36     <none>                                                                  443/TCP                      76m
```
Take note of the `LoadBalancer` under the `EXTERNAL-IP` column, using it as a value update the DNS record so that traffic is routed to it.

> **NOTE:** It can take a few minutes for the DNS to resolve these changes.

## Certificate issuer installation and configuration
Now that the Ingress controller is setup, it can be configured for TLS termination by configuring a Ingress resource.

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
    cert-manager.io/issuer: "letsencrypt-prod" # Using [Let's Encrypt](https://letsencrypt.org/
  https: true
  tlsSecretName: tls-certificate
```

### 2. Install certificate manager
K8s certificate management is handled using cert-manager(https://cert-manager.io/)

Create a new namespace for the cert-manager resources
```shell
kubectl create namespace cert-manager
```

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

### 3. Create certificate issuer
Using the yaml spec below create the certificate `Issuer` resource
> Ensure that the certificate issuer s installed in the same namespace that the Atlassian product will be deployed to.

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

> Having created the Ingress controller continue with provisioning the [pre-requisite infrastructure](../../PREREQUISITES.md).