# Logging in a Kubernetes environment

!!!warning Disclaimer
    **This functionality is not officially supported.** This document explains how to enable aggregated logging in your Kubernetes cluster. There are many ways to do this and this document showcases only a few of the options.

## EFK stack

A common Kubernetes logging pattern is the combination of `Elasticsearch`, `Fluentd`, and `Kibana`, known as *EFK Stack*. 

[Fluentd](https://www.fluentd.org/){.external} is an open-source and multi-platform log processor that collects data/logs from different sources, aggregates, and forwards them to multiple destinations. It is fully compatible with Docker and Kubernetes environments. 

[Elasticsearch](https://www.elastic.co/){.external} is a distributed open search and analytics engine for all types of data. 

[Kibana](https://www.elastic.co/kibana/){.external} is an open-source front-end application that sits on top of Elasticsearch, providing search and data visualization capabilities for data indexed in Elasticsearch.

There are different methods to deploy an EFK stack. We provide two deployment methods, the first is deploying EFK locally on Kubernetes, and the second is using a managed Elasticsearch instance outside the Kubernetes cluster. 

## Local EFK stack

This solution deploys the EFK stack inside the Kubernetes cluster. By setting `fluentd.enabled` value to `true`, Helm installs Fluentd on each of application pods. This means that after deployment all the product pods run Fluentd, which collects all the log files and sends them to the Fluentd aggregator container. 

To complete the EFK stack you need to install an Elasticsearch cluster and Kibana, and successfully forward the aggregated datalog to Elasticsearch using Fluentd, which is already installed. 

Follow these steps to install Elasticsearch

### 1. Install Elasticsearch

Install Elasticsearch using the instructions [documented here](../../bitbucket/BITBUCKET_ELASTICSEARCH.md). Once installed make sure Elasticsearch cluster is working as expected by first port forwarding the service

```shell
kubectl port-forward svc/elasticsearch-master 9200
```

you can then `curl` the endpoint for the current state

```shell
$ curl localhost:9200
{
  "name" : "elasticsearch-master-0",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "uNdYC-2nSdWVdzPCw9P7jQ",
  "version" : {
       "number" : "7.12.0",
       "build_flavor" : "default",
       "build_type" : "docker",
       "build_hash" : "78722783c38caa25a70982b5b042074cde5d3b3a",
       "build_date" : "2021-03-18T06:17:15.410153305Z",
       "build_snapshot" : false,
       "lucene_version" : "8.8.0",
       "minimum_wire_compatibility_version" : "6.8.0",
       "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```

### 2. Enable Fluentd

Now enable `Fluentd` and set the `hostname` for Elasticsearch in `values.yaml` as follows:

```yaml
fluentd:
   enabled: true
   elasticsearch:
     hostname: elasticsearch-master
```
Fluentd tries to parse and send the data to Elasticsearch, but since it's not installed the data is lost. At this point you have logged data in the installed Elasticsearch, and you should install Kibana to complete the EFK stack deployment:

### 3. Install Kibana
With the same version that was used for installing [Elasticsearch](../../elasticsearch/BITBUCKET_ELASTICSEARCH.md), use the `imageTag` property to install Kibana:

```shell
helm install kibana --namespace <namespace> --set imageTag="7.9.3" elastic/kibana
```

Make sure kibana is running by checking the deployment

```shell
kubectl get deployment
```
You should see something like...
```shell
NAME                               READY   UP-TO-DATE   AVAILABLE   AGE
helm-operator                      1/1           1            1     23m
ingress-nginx-release-controller   1/1           1            1     22m
kibana-kibana                      1/1           1            1     25m
```
Through port-forwarding you can access Kibana via `http://localhost:5601`
```shell
kubectl port-forward deployment/kibana-kibana 5601
```
 To visualise the logs you need to create an index pattern and then look at the the data in the discovery part. To create the index pattern go to `Management` → `Stack Management` and then select `Kibana` → `Index Patterns`. 

## Managed EFK stack

In this solution [Elasticsearch is deployed as a managed AWS service](https://aws.amazon.com/elasticsearch-service/){.external} and lives outside of the Kubernetes cluster. This approach uses [Fluentbit](https://fluentbit.io/){.external} instead of `Fluentd` for log processing.

???+ info "Fluentbit"

    `Fluentbit` is used to collect and aggregate log data inside the EKS cluster. It then sends this to an AWS Elasticsearch instance outside of the cluster.


When a node inside an EKS cluster needs to call an AWS API, it needs to provide extended permissions. Amazon provides an image of `Fluentbit` that supports AWS service accounts,and using this you no longer need to follow the traditional way. All you need is to have an IAM role for the AWS service account on an EKS cluster. Using this service account, an AWS permission can be provided to the containers in any pod that use that service account. The result is that the pods on that node can call AWS APIs.

Your first step is to configure IAM roles for Service Accounts (IRSA) for `Fluentbit`, to make sure you have an OIDC identity provider to use IAM roles for the service account in the cluster:

```shell
eksctl utils associate-iam-oidc-provider \
     --cluster <cluster_name> \
     --approve 
```
Then create an IAM policy to limit the permissions to connect to the Elasticsearch cluster. Before this, you need to set the following environment variables: 

| Environment variable  | Value                                                                              |                                                                       
|-----------------------|------------------------------------------------------------------------------------|
| KUBE_NAMESPACE        | The namespace for kubernetes cluster                                               |
| ES_DOMAIN_NAME        | Elasticsearch domain name                                                          |        
| ES_VERSION            | Elasticsearch version                                                              |
| ES_USER               | Elasticsearch username                                                             | 
| ES_PASSWORD           | Elasticsearch password (eg. `export ES_PASSWORD="$(openssl rand -base64 8)_Ek1$"`) |
| ACCOUNT_ID            | AWS Account ID                                                                     |
| AWS_REGION            | AWS region code                                                                    |

Now create the file `fluent-bit-policy.json` to define the policy itself:

```shell
cat <<EoF > ~/environment/logging/fluent-bit-policy.json
{
    "Version": "2012-10-17",
    "Statement": [
         {
             "Action": [
                 "es:ESHttp*"
             ],
             "Resource": "arn:aws:es:${AWS_REGION}:${ACCOUNT_ID}:domain/${ES_DOMAIN_NAME}",
             "Effect": "Allow"
         }
    ]
}
EoF
```
Next initialize the policy:
```shell
aws iam create-policy  \
     --policy-name fluent-bit-policy \
     --policy-document file://~/environment/logging/fluent-bit-policy.json
```
Create an IAM role for the service account:
```shell
eksctl create iamserviceaccount \
     --name fluent-bit \
     --namespace dcd \
     --cluster dcd-ap-southeast-2 \
     --attach-policy-arn "arn:aws:iam::${ACCOUNT_ID}:policy/fluent-bit-policy" \
     --approve \
     --override-existing-serviceaccounts
```
Confirm that the service account with an Amazon Resource Name (ARN) of the IAM role is annotated:
```shell
kubectl describe serviceaccount fluent-bit
```
Look for output similar to:
```yaml
Name: fluent-bit
Namespace:  elastic
Labels: <none>
Annotations: eks.amazonaws.com/role-arn: arn:aws:iam::000000000000:role/eksctl-your-cluster-name-addon-iamserviceac-Role1-0A0A0A0A0A0A0
Image pull secrets: <none>
Mountable secrets:  fluent-bit-token-pgpss
Tokens:  fluent-bit-token-pgpss
Events:  <none>
```
Now define the Elasticsearch domain

!!!info ""

    This configuration will provision a public Elasticsearch cluster with Fine-Grained Access Control enabled and a built-in user database:

```shell
cat <<EOF> ~/environment/logging/elasticsearch_domain.json
{
    "DomainName": ${ES_DOMAIN_NAME},
    "ElasticsearchVersion": ${ES_VERSION},
    "ElasticsearchClusterConfig": {
         "InstanceType": "r5.large.elasticsearch",
         "InstanceCount": 1,
             "DedicatedMasterEnabled": false,
             "ZoneAwarenessEnabled": false,
             "WarmEnabled": false
         },
    "EBSOptions": {
         "EBSEnabled": true,
         "VolumeType": "gp2",
         "VolumeSize": 100
    },
    "AccessPolicies": "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":\"*\"},\"Action\":\"es:ESHttp*\",\"Resource\":\"arn:aws:es:${AWS_REGION}:${ACCOUNT_ID}:domain/${ES_DOMAIN_NAME}/*\"}]}",
    "SnapshotOptions": {},
    "CognitoOptions": {
         "Enabled": false
    },
    "EncryptionAtRestOptions": {
         "Enabled": true
    },
    "NodeToNodeEncryptionOptions": {
         "Enabled": true
    },
    "DomainEndpointOptions": {
         "EnforceHTTPS": true,
         "TLSSecurityPolicy": "Policy-Min-TLS-1-0-2019-07"
    },
    "AdvancedSecurityOptions": {
         "Enabled": true,
         "InternalUserDatabaseEnabled": true,
         "MasterUserOptions": {
             "MasterUserName": ${ES_USER},
             "MasterUserPassword": ${ES_PASSWORD}
         }
    }
}
EOF
```
Initialize the Elasticsearch domain using the `elasticsearch_domain.json`

```shell
aws es create-elasticsearch-domain \
   --cli-input-json   file://~/environment/logging/elasticsearch_domain.json
```

!!!info ""
    
    It takes a while for Elasticsearch clusters to change to an active state. Check the AWS Console to see the status of the cluster, and continue to the next step when the cluster is ready.

At this point you need to map roles to users in order to set fine-grained access control, because without this mapping all the requests to the cluster will result in permission errors. You should add the `Fluentbit` ARN as a backend role to the `all-access` role, which uses the Elasticsearch APIs. To find the `fluentbit` ARN run the following command and export the value of `ARN Role` into the `FLUENTBIT_ROLE` environment variable:
```shell
eksctl get iamserviceaccount --cluster dcd-ap-southeast-2
```
The output of this command should look similar to this:
```shell
NAMESPACE    NAME                ROLE ARN
kube-system cluster-autoscaler   arn:aws:iam::887464544476:role/eksctl-dcd-ap-southeast-2-addon-iamserviceac-Role1-1RSRFV0BQVE3E
```
Take note of the `ROLE ARN` and export it as the environment variable `FLUENTBIT_ROLE`
```shell
export FLUENTBIT_ROLE=arn:aws:iam::887464544476:role/eksctl-dcd-ap-southeast-2-addon-iamserviceac-Role1-1RSRFV0BQVE3E
```
Retrieve the Elasticsearch endpoint and update the internal database:
```shell
export ES_ENDPOINT=$(aws es describe-elasticsearch-domain --domain-name ngh-search-domain --output text --query "DomainStatus.Endpoint")
```

```shell
curl -sS -u "${ES_DOMAIN_USER}:${ES_DOMAIN_PASSWORD}" \
   -X PATCH \
   https://${ES_ENDPOINT}/_opendistro/_security/api/rolesmapping/all_access?pretty \
   -H 'Content-Type: application/json' \
   -d'
[
   {
     "op": "add", "path": "/backend_roles", "value": ["'${FLUENTBIT_ROLE}'"]
   }
]
'
```
Finally, it is time to deploy the `Fluentbit` DaemonSet:
```shell
kubectl apply -f docs/docs/examples/logging/efk/managed_es/fluentbit.yaml
```
After a few minutes all pods should be up and in running status. you can open Kibana to visualise the logs. The endpoint for Kibana can be found in the Elasticsearch output tab in the AWS console, or you can run the following command:
```shell
echo "Kibana URL: https://${ES_ENDPOINT}/_plugin/kibana/" 
Kibana URL: https://search-domain-uehlb3kxledxykchwexee.ap-southeast-2.es.amazonaws.com/_plugin/kibana/
```

The user and password for Kibana are the same as the master user credential that is set in Elasticsearch in the provisioning stage. Open Kibana in a browser and after login, create an index pattern and see the report in the `Discover` page. 
