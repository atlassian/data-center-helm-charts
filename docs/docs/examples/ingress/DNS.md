# Create DNS record via AWS CLI

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