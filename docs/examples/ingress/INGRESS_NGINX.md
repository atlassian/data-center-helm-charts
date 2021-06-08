# NGINX Ingress Controller on AWS - provisioning and usage
> **NOTE:** These instructions are for reference purposes only. They are used for development and testing purposes only and are specific to AWS deployments.  

You can refer to the official instructions for [deploying and configuring the controller](https://kubernetes.github.io/ingress-nginx/deploy/).

## Prerequisites
We performed testing on AWS, so these prerequisites and instructions are tailored toward it. However, at a high level, the same details will apply to other cloud providers.

1. Provision a new DNS record (Amazon Route53). This DNS record will be used to configure the Ingress resource, see [Ingress resource config](#Ingress-resource-config) below.
2. Provision a new TLS certificate (Amazon Certificate Manager) for the DNS record.

## Controller installation
We recommend installing the controller using its official [Helm charts](https://github.com/kubernetes/ingress-nginx/tree/master/charts/ingress-nginx). You can also use the instructions below, which are based on the official ones.

### 1. Add repo
Add the `ingress-nginx` Helm repo
```shell
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
```

### 2. Create a custom config yaml
Appropriately update the provided config below. This configuration will auto-provision an AWS L7 ELB (Application Load Balancer) with SSL Termination.
> Ensure the appropriate ARN is provided for the TLS certificate created under [Prerequisites](#Prerequisites)
```yaml
controller:
  config:
    use-forwarded-headers: "true"
  service:
    targetPorts:
      http: http
      https: http
    annotations:
      service.beta.kubernetes.io/aws-load-balancer-ssl-cert:  "<arn_for_tls_cert>"
      service.beta.kubernetes.io/aws-load-balancer-backend-protocol: "http"
      service.beta.kubernetes.io/aws-load-balancer-ssl-ports: "https"
      service.beta.kubernetes.io/aws-load-balancer-connection-idle-timeout: '3600'
      service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags: "<tag1>=<tag1_value>,<tag2>=<tag2_value>,etc..."
```
> **NOTE:** Bitbucket requires additional config to allow for `SSH` access. See [SSH service in Bitbucket](../ssh/SSH_BITBUCKET.md) for the instructions.

### 3. Install
Using the update config in [Step 2](#Update-config.yaml) install the controller
```shell
helm install <release name> ingress-nginx/ingress-nginx --values ingress-config.yaml
```

### 4. DNS record to loadbalancer wiring
Once the controller is installed you will need to associate the DNS record created as part of the [Prerequisites](#Prerequisites) with the auto-provisioned AWS LB that was created when installing the controller. To do this first identify the name of the auto-provisioned LB, this can be done by examining the deployed ingress services i.e.
```shell
kubectl get service | grep ingress-nginx    
```
The output of this command should look something like:
```shell
NAME                                 TYPE           CLUSTER-IP      EXTERNAL-IP
ingress-nginx-controller             LoadBalancer   10.100.22.16    b834z142d8118406795a34df35e10b17-38927090.eu-west-1.elb.amazonaws.com   80:32615/TCP,443:31787/TCP   76m
ingress-nginx-controller-admission   ClusterIP      10.100.5.36     <none>                                                                  443/TCP                      76m
```
Take note of the `LoadBalancer` under the `EXTERNAL-IP` column, using it as a value update the DNS record in Route53 so that it routes traffic to it.

> **NOTE:** It can take a few minutes for the DNS to resolve these changes.   

## Ingress resource config
Prior to [installing](../../INSTALLATION.md) the Atlassian products using the Helm charts, ensure that the `ingress` stanza within the product `values.yaml` file has been updated accordingly for the `ingress-nginx` controller. These properties will configure an appropriate ingress resource for the controller, i.e.
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
The controller can be uninstalled by running
```shell
helm uninstall <release name>
```

> Once you have created the Ingress controller continue with provisioning the [prerequisite infrastructure](../../PREREQUISITES.md).
