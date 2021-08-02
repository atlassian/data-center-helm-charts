# Logging in a Kubernetes environment

!!!warning Disclaimer
    **This functionality is not officially supported.** This document explains how to enable aggregated logging in your Kubernetes cluster. There are many ways to do this and this document showcases only a few of the options.

## EFK stack

A common Kubernetes logging pattern is the combination of ***Elasticsearch***, ***Fluentd***, and ***Kibana***, known as *EFK Stack*. 

`Fluentd` is an open-source and multi-platform log processor that collects data/logs from different sources, aggregates, and forwards them to multiple destinations. It is fully compatible with Docker and Kubernetes environments. 

`Elasticsearch` is a distributed open search and analytics engine for all types of data. 

`Kibana` is an open-source front-end application that sits on top of Elasticsearch, providing search and data visualization capabilities for data indexed in Elasticsearch.

There are different methods to deploy an EFK stack. We provide two deployment methods, the first is deploying EFK locally on Kubernetes, and the second is using managed Elasticsearch outside the Kubernetes cluster. 

## EFK using local Elasticsearch

This solution deploys all of the EFK stack inside the Kubernetes cluster. By setting `fluentd.enabled` value to `true`, Helm installs Fluentd on each of application pods. This means that after deployment all the product pods run Fluentd, which collects all the log files and sends them to the Fluentd aggregator container. 

To complete the EFK stack you need to install an Elasticsearch cluster and Kibana, and successfully forward the aggregated datalog to Elasticsearch using Fluentd, which is already installed. 

Follow these steps to install Elasticsearch:

```shell
$ helm repo add elastic https://helm.elastic.co
"elastic" has been added to your repositories

$ helm install elasticsearch elastic/elasticsearch
...
```

Wait until all the nodes start and the status changes to `Running`:

```shell
$ kubectl get pods --namespace=dcd -l app=elasticsearch-master
NAME                     READY   STATUS    RESTARTS   AGE
elasticsearch-master-0   1/1     Running   0          100m
elasticsearch-master-1   1/1     Running   0          100m
elasticsearch-master-2   1/1     Running   0          100m
$
$ kubectl port-forward svc/elasticsearch-master 9200
```
Make sure Elasticsearch cluster is working as expected:

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

Now enable `fluentd` and set the `hostname` for Elasticsearch in `values.yaml` as follows:

```yaml
fluentd:
   enabled: true
   elasticsearch:
     hostname: elasticsearch-master
```
Fluentd tries to parse and send the data to Elasticsearch, but since it's not installed yet the data is lost. At this point you have logged data in the installed Elasticsearch, and you should install Kibana to complete the EFK stack deployment:

```shell
$ helm install kibana elastic/kibana
NAME:   kibana
LAST DEPLOYED: Wed Apr 14 12:52:21 2021
NAMESPACE: dcd
STATUS: DEPLOYED
```
Make sure kibana is deployed and then setup port forwarding for kibana:
```shell
$ kubectl get deployment
NAME                               READY   UP-TO-DATE   AVAILABLE   AGE
helm-operator                      1/1           1            1     23m
ingress-nginx-release-controller   1/1           1            1     22m
kibana-kibana                      1/1           1            1     25m

$ kubectl port-forward deployment/kibana-kibana 5601
```
You can access Kibana via the browser: http://localhost:5601. To visualise the logs you need to create an index pattern and then look at the the data in the discovery part. To create the index pattern go to `Management` → `Stack Management` and then select `Kibana` → `Index Patterns`. 

## Managed Elasticsearch in AWS

In this solution Elasticsearch deploys as a managed service and lives outside of the Kubernetes cluster. For this purpose use Fluent Bit instead of Fluentd for local deployment of EFK. 

When a node inside an EKS cluster needs to call an AWS API, it needs to provide extended permissions. Amazon provides an image of Fluent Bit that supports AWS service accounts,and using this you no longer need to follow the traditional way. All you need is to have an IAM role for the AWS service account on an EKS cluster. So using this service account, an AWS permission can be provided to the containers in any pod that use that service account. The result is that the pods on that node can call AWS APIs.

`fluentbit` is used to collect and aggregate the data inside the EKS cluster, which communicates with AWS Elasticsearch outside of the cluster. 

Your first step is to configure IAM roles for Service Accounts (IRSA) for `fluentbit`, to make sure you have an OIDC identity provider to use IAM roles for the service account in the cluster:

```shell
$ eksctl utils associate-iam-oidc-provider \
     --cluster dcd-ap-southeast-2 \
     --approve 
```
Then create an IAM policy to limit the permissions to connect to the Elasticsearch cluster. Before this, you need to set the following environment variables: 
* KUBE_NAMESPACE : The namespace for kubernetes cluster
* ES_DOMAIN_NAME : Elasticsearch domain name
* ES_VERSION : Elasticsearch version 
* ES_USER : Elasticsearch username
* ES_PASSWORD : Elasticsearch password (eg. `export ES_PASSWORD="$(openssl rand -base64 8)_Ek1$"`)
* ACCOUNT_ID : AWS Account ID
* AWS_REGION : AWS region code
```shell
$ mkdir ~/environment/logging
$ cat <<EoF > ~/environment/logging/fluent-bit-policy.json
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

$ aws iam create-policy  \
     --policy-name fluent-bit-policy \
     --policy-document file://~/environment/logging/fluent-bit-policy.json
```
Next, create an IAM role for the service account:

```shell
eksctl create iamserviceaccount \
     --name fluent-bit \
     --namespace dcd \
     --cluster dcd-ap-southeast-2 \
     --attach-policy-arn "arn:aws:iam::${ACCOUNT_ID}:policy/fluent-bit-policy" \
     --approve \
     --override-existing-serviceaccounts
```

To confirm that the service account with an Amazon Resource Name (ARN) of the IAM role is annotated:
```shell
$ kubectl describe serviceaccount fluent-bit
Name: fluent-bit
Namespace:  dcd
Labels: <none>
Annotations: eks.amazonaws.com/role-arn: arn:aws:iam::887464544476:role/eksctl-dcd-ap-southeast-2-addon-iamserviceac-Role1-9B18FAAFE02F6
Image pull secrets: <none>
Mountable secrets:  fluent-bit-token-pgpss
Tokens:  fluent-bit-token-pgpss
Events:  <none>
```

*Provision an Elasticsearch cluster:* Provision a public Elasticsearch cluster with Fine-Grained Access Control enabled and a built-in user database:
```shell
$ cat <<EOF> ~/environment/logging/elasticsearch_domain.json
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

$ aws es create-elasticsearch-domain \
   --cli-input-json   file://~/environment/logging/es_domain.json
```

It takes a while for Elasticsearch clusters to change to an active state. Check the AWS Console to see the status of the cluster, and continue to the next step when the cluster is ready.

*Configure Elasticsearch access:* At this point you need to map roles to users in order to set fine-grained access control, because without this mapping all the requests to the cluster will result in permission errors. You should add the Fluent Bit ARN as a backend role to the `all-access` role, which uses the Elasticsearch APIs. To find the Fluent Bit ARN run the following command and export the value of `ARN Role` into the `FLUENTBIT_ROLE` environment variable:
```shell
$ eksctl get iamserviceaccount --cluster dcd-ap-southeast-2
[ℹ] eksctl version 0.37.0
[ℹ] using region ap-southeast-2
NAMESPACE    NAME                ROLE ARN
kube-system cluster-autoscaler   arn:aws:iam::887464544476:role/eksctl-dcd-ap-southeast-2-addon-iamserviceac-Role1-1RSRFV0BQVE3E

$ export FLUENTBIT_ROLE=arn:aws:iam::887464544476:role/eksctl-dcd-ap-southeast-2-addon-iamserviceac-Role1-1RSRFV0BQVE3E
```
Retrieve the Elasticsearch endpoint and update the internal database:
```shell
$ export ES_ENDPOINT=$(aws es describe-elasticsearch-domain --domain-name ngh-search-domain --output text --query "DomainStatus.Endpoint")
$ curl -sS -u "${ES_DOMAIN_USER}:${ES_DOMAIN_PASSWORD}" \
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
Finally, it is time to deploy Fluent Bit DaemonSet:
```shell
$ kubectl apply -f src/main/logging/fluentbit.yaml
```
After a few minutes all pods should be up and in running status. This is the end of the, you can open Kibana to visualise the logs. The endpoint for Kibana can be found in the Elasticsearch output tab in the AWS console, or you can run the following command:
```shell
$ echo "Kibana URL: https://${ES_ENDPOINT}/_plugin/kibana/" 
Kibana URL: https://search-domain-uehlb3kxledxykchwexee.ap-southeast-2.es.amazonaws.com/_plugin/kibana/
```

The user and password for Kibana are the same as the master user credential that is set in Elasticsearch in the provisioning stage. Open Kibana in a browser and after login, create an index pattern and see the report in the `Discover` page. 
