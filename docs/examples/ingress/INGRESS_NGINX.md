# NGINX Ingress Controller - Provisioning and usage
> **NOTE:** These instructions are for reference purposes only. They are used for development and testing purposes only and are specific to AWS deployments.  

Official instructions for deploying and configuring the controller can be found [here](https://kubernetes.github.io/ingress-nginx/deploy/).

## Prerequisites
Testing was performed on AWS and so these prerequisites and instructions are tailored toward it. However, at a high level, the same details will apply to other cloud providers.

1. Provision a new DNS record (Amazon Route53). This DNS record will be used to configure the Ingress resource, see [Ingress resource config](#Ingress-resource-config) below.
2. Provision a new TLS certificate (Amazon Certificate Manager) for the DNS record.

## Controller installation
We recommend installing the controller using its official [Helm Charts](https://github.com/kubernetes/ingress-nginx/tree/master/charts/ingress-nginx). You can also use the instructions below which are based on the official ones.

### 1. Add repo
Add the `ingress-nginx` Helm repo
```shell
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
```

### 2. Create a custom config yaml
Appropriately update the provided config below. This configuration will auto provision an AWS L7 ELB (Application Load Balancer) with SSL Termination.
> Ensure the appropriate ARN is supplied for the TLS certificate created under [Prerequisites](#Prerequisites)
```yaml
controller:
  config:
    use-proxy-protocol: "true"
    http-snippet: |
      server {
        listen 8080 proxy_protocol;
        return 308 https://$host$request_uri;
      }
  service:
    internal:
      enabled: true
    targetPorts:
      http: 8080
      https: http
    annotations:
        "service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags": "<tag1>=<tag1_value>,<tag2>=<tag2_value>,etc..."
        "service.beta.kubernetes.io/aws-load-balancer-backend-protocol": "tcp"
        "service.beta.kubernetes.io/aws-load-balancer-proxy-protocol": "*"
        "service.beta.kubernetes.io/aws-load-balancer-ssl-ports": "https"
        "service.beta.kubernetes.io/aws-load-balancer-ssl-cert": "<arn_for_tls_cert>"
    externalTrafficPolicy: "Local"
```
> **NOTE:** Bitbucket requires additional config to allow for `SSH` access. See [here](????????) for detailed instructions.

### 3. Install
Using the update config in [Step 2.](#Update-config.yaml) install the controller
```shell
helm install <release name> ingress-nginx/ingress-nginx --values ingress-config.yaml
```

### 4. DNS record to LB wiring
Once the controller is installed you will need to associate the DNS record created as part of the [Prerequisites](#Prerequisites) with the auto provisioned AWS LB that was created when installing the controller. To do this first identify the name of the auto provisioned LB, this can be done by examining the deployed ingress services i.e.
```shell
kubectl get service | grep ingress-nginx    
```
the output of this command should look something like...
```shell
NAME                                 TYPE           CLUSTER-IP      EXTERNAL-IP
ingress-nginx-controller             LoadBalancer   10.100.22.16    b834z142d8118406795a34df35e10b17-38927090.eu-west-1.elb.amazonaws.com   80:32615/TCP,443:31787/TCP   76m
ingress-nginx-controller-admission   ClusterIP      10.100.5.36     <none>                                                                  443/TCP                      76m
```
Take note of the `LoadBalancer` under the `EXTERNAL-IP` column, using it as a value update the DNS record in Route53 so that it routes traffic to it.

## Ingress resource config
Prior to [installing](../../INSTALLATION.md) the Atlassian products using the Helm charts, ensure that the `ingress` stanza within the product `values.yaml` has been updated accordingly for the `ingress-nginx` controller. These properties will configure an appropriate ingress resource for the controller i.e.
```yaml
ingress:
  create: true
  nginx: true
  maxBodySize: 250m
  host: <dns record>
  path: "/"
  https: true
```

## Uninstall
The controller can be un-installed by running
```shell
helm uninstall <release name>
```

> Having created the Ingress controller continue with provisioning the [pre-requisite infrastructure](../../PREREQUISITES.md).