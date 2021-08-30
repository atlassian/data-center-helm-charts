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
Manually provision a new DNS record via your cloud provider or dynamically using [external-dns](https://github.com/kubernetes-sigs/external-dns/blob/master/docs/tutorials/aws.md){.external}. Alternatively the instructions below show how this can be performed using [AWS Route53](https://aws.amazon.com/route53/){.external}

!!!tip "DNS record creation using Route53"

    The approach below shows how a DNS record can be created using AWS Route53 and the [AWS CLI for record sets](https://aws.amazon.com/premiumsupport/knowledge-center/alias-resource-record-set-route53-cli/){.external}

First, identify the name of the auto provisioned [AWS Classic Load Balancer](https://aws.amazon.com/elasticloadbalancing/classic-load-balancer/){.external} that was created above for [Step 2. Install controller](#2-install-controller):
```shell
kubectl get service -n ingress | grep ingress-nginx | awk '{print $4}' | head -1
```
the output of this command should be the name of the load balancer, take note of the name i.e.
```shell
b834z142d8118406795a34df35e10b17-38927090.eu-west-1.elb.amazonaws.com
```
Next, using the first part of the load balancer name, get the `HostedZoneId` for the load balancer
```shell
aws elb describe-load-balancers --load-balancer-name b834z142d8118406795a34df35e10b17 --region <aws_region> | jq '.LoadBalancerDescriptions[] | .CanonicalHostedZoneNameID'
```
With the `HostedZoneId` and the **full** name of the load balancer create the `JSON` "change batch" file below:

```yaml
{
  "Comment": "An alias resource record for Jira in K8s",
  "Changes": [
    {
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": <DNS record name>,
        "Type": "A",
        "AliasTarget": {
          "HostedZoneId": <Load balancer hosted zone ID>,
          "DNSName": <Load balancer name>,
          "EvaluateTargetHealth": true
        }
      }
    }
  ]
}
```
  
!!!tip "DNS record name"

    If for example, the DNS record name were set to `product.k8s.hoolicorp.com` then the host, `hoolicorp.com`, would be the pre-registerd [AWS Route53 hosted zone](https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/route-53-concepts.html#route-53-concepts-hosted-zone){.external}.

Next get the zone ID for the hosted zone:
```shell
aws route53 list-hosted-zones-by-name | jq '.HostedZones[] | select(.Name == "hoolicorp.com.") | .Id'
```
Finally, using the hosted zone ID and the `JSON` change batch file created above, initialize the record:
```shell
aws route53 change-resource-record-sets --hosted-zone-id <hosted zone ID> --change-batch file://change-batch.json
```
This will return a response similar to the one below:
```json
{
    "ChangeInfo": {
        "Id": "/change/C03268442VMV922ROD1M4",
        "Status": "PENDING",
        "SubmittedAt": "2021-08-30T01:42:23.478Z",
        "Comment": "An alias resource record for Jira in K8s"
    }
}
```
You can get the current status of the record's initialization:
```shell
aws route53  get-change --id /change/C03268442VMV922ROD1M4
```
Once the `Status` has transitioned to `INSYNC` the record is ready for use...
```json
{
    "ChangeInfo": {
        "Id": "/change/C03268442VMV922ROD1M4",
        "Status": "INSYNC",
        "SubmittedAt": "2021-08-30T01:42:23.478Z",
        "Comment": "Creating Alias resource record sets in Route 53"
    }
}
```

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

!!!tip "Configuring the `host` value"

    In this case the `<dns_record>` would correspond to the record name that was created in [3. DNS setup](#3-dns-setup) above



## Bitbucket SSH configuration
> **NOTE:** Bitbucket requires additional Ingress config to allow for `SSH` access. See [NGINX Ingress controller config for SSH connections](../ssh/SSH_BITBUCKET.md) for details.

> Having created the Ingress controller continue with provisioning the [prerequisite infrastructure](../../userguide/PREREQUISITES.md).
