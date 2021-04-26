## Deploy Logging Pattern - EFK Stack

A common Kubernetes logging pattern is a combination of Fluent, Elasticsearch, and Kibana which is known as *EFK Stack*. 

`Fluent` is an open source and multi-platform log processor which collects data/log from different sources, aggregates and forwards them to multiple destinations and is fully compatible with Docker and Kubernetes environments. 

`Elasticsearch` is a distributed open search and analytics engine for all types of data. 

`Kibana` is an open source frontend application that sits on top of Elasticsearch, providing search and data visualization capabilities for data indexed in Elasticsearch.

There are different methods to deploy EFK stack and here we provide two deployment methods, first deploy EFK using helm chart and deploy local elasticsearch on Kubernetes and second using managed elasticsearch (EKS and AWS ES).

## EFK using local elasticsearch

This solution deploys all EFK stack inside the k8s cluster locally. By enabling fluentd, helm_install script installs fluentd in EKS. It means after running helm_install all product pods will run fluentd which collects all log files and sends them to the fluentd aggregator container. 

To complete the EFK stack we need to install elasticsearch cluster and kibana and manage to forward the aggregated datalog to elasticsearch by fluentd which is already installed. 

To start first enable `fluentd` and set the `hostname` for elasticsearch in volues.yaml as follows:

```yaml
fluentd:
   enabled: true
   elasticsearch:
     hostname: elasticsearch-master
```

Now fluentd tries to parse and send the data to elasticsearch but as it is not installed yet then data get lost. At this point we should install elasticsearch and kibana to complete EFK stack deployment.

```shell script
$ helm repo add elastic https://helm.elastic.co
"elastic" has been added to your repositories

$ helm install elasticsearch elastic/elasticsearch
...
```

wait until all nodes get up and running status:

```shell script
$ kubectl get pods --namespace=dcd -l app=elasticsearch-master
NAME                     READY   STATUS    RESTARTS   AGE
elasticsearch-master-0   1/1     Running   0          100m
elasticsearch-master-1   1/1     Running   0          100m
elasticsearch-master-2   1/1     Running   0          100m
$
$ kubectl port-forward svc/elasticsearch-master 9200
```
make sure elasticsearch cluster working as expected:
```shell script
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
Now is time to install Kibana:
```shell script
$ helm install kibana elastic/kibana
NAME:   kibana
LAST DEPLOYED: Wed Apr 14 12:52:21 2021
NAMESPACE: dcd
STATUS: DEPLOYED
```
make sure kibana is deployed and then setup port forwarding for kibana:
```shell script
$ kubectl get deployment
NAME                               READY   UP-TO-DATE   AVAILABLE   AGE
helm-operator                      1/1     1            1           23d
ingress-nginx-release-controller   1/1     1            1           82d
kibana-kibana                      1/1     1            1           125m

$ kubectl port-forward deployment/kibana-kibana 5601
```
Now kibana is accessible in the browser by: http://localhost:5601. To visualise the logs you need to create an index pattern and then see the data in the discovery part. To create the index pattern go to `Management` → `Stack Management` and then select `Kibana` → `Index Patterns`. 

## EFK in EKS using managed AWS Elasticsearch

In this method elastic search will deploy out of k8s clusters. For this purpose we use fluent bit instead of fluentd which is used for locally deployment for EFK. 

When a node inside an EKS cluster needs to call AWS APIs, it needs to provide extended permissions. Amazon provided an image of fluent-bit which supports service accounts and using this we no longer need to follow the traditional way. All we need is to have an IAM role for the service account on EKS cluster so using this service account we can provide AWS permission to the containers in any pod that use that service account. So the pods on that node can call AWS APIs.

`fluentbit` is used to collect and aggregate the data inside the EKS cluster which will communicate with AWS elasticsearch outside of the cluster. 

First step is configure IRSA for `fluentbit` to make sure we have OIDC identity provider to use IAM roles for the service account in the cluster:

```shell script
$ eksctl utils associate-iam-oidc-provider \
     --cluster dcd-ap-southeast-2 \
     --approve 
```
Then create an IAM policy to limit the permissions to connect to the elasticsearch cluster, but before this we need to set the following environment variables: 
* ES_DOMAIN_NAME : Elasticsearch domain name
* ES_VERSION : Elasticsearch version 
* ES_USER : Elasticsearch username
* ES_PASSWORD : Elasticsearch password (eg. `export ES_PASSWORD="$(openssl rand -base64 8)_Ek1$"`)
* ACCOUNT_ID : AWS Account ID
* AWS_REGION : AWS region code
```shell script
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
Next create an IAM role for the service account:

```shell script
eksctl create iamserviceaccount \
     --name fluent-bit \
     --namespace dcd \
     --cluster dcd-ap-southeast-2 \
     --attach-policy-arn "arn:aws:iam::${ACCOUNT_ID}:policy/fluent-bit-policy" \
     --approve \
     --override-existing-serviceaccounts
```

To confirm that service account with arn of the IAM role is annotated:
```shell script
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

*Provision an elasticsearch cluster:*  Now it is time to provision a public elasticsearch cluster with Fine-Grained Access Control enabled and a built-in user database:
```shell script
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

It takes a while for elasticsearch clusters to be in an active state. Check AWS Console to see the status of the cluster and continue to the next step when the cluster is ready.

*Configure elasticsearch access:* At this point we need to map roles to users in order to set fine-grained access control because without this mapping all requests to the cluster will result in a permission error. We should add the fluent bit ARN as a backend role to the `all-access` role which is using the elasticsearch APIs. To find the fluent bit ARN  run the following command and export the value of `ARN Role` in to `FLUENTBIT_ROLE` environment variable:
```shell script
$ eksctl get iamserviceaccount --cluster dcd-ap-southeast-2
[ℹ] eksctl version 0.37.0
[ℹ] using region ap-southeast-2
NAMESPACE    NAME                ROLE ARN
kube-system cluster-autoscaler   arn:aws:iam::887464544476:role/eksctl-dcd-ap-southeast-2-addon-iamserviceac-Role1-1RSRFV0BQVE3E

$ export FLUENTBIT_ROLE=arn:aws:iam::887464544476:role/eksctl-dcd-ap-southeast-2-addon-iamserviceac-Role1-1RSRFV0BQVE3E
```
then retrieve elasticsearch endpoint and update the internal database:
```shell script
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
Finally, it is time to deploy fluent bit cluster:
```shell script
$ kubectl apply -f src/main/logging/fluentbit.yaml
```
After a few minutes all pods should get up and in running status. Now all steps are completed and you can open kibana to visualise the logs. The endpoint for kibana could be found in elasticsearch output tab in AWS console or run the following command:
```shell script
$ echo "Kibana URL: https://${ES_ENDPOINT}/_plugin/kibana/" 
Kibana URL: https://search-domain-uehlb3kxledxykchwexee.ap-southeast-2.es.amazonaws.com/_plugin/kibana/
```

The user and password for kibana is the same master user credential which is set in elasticsearch in the provisioning stage. Open kibana in a browser and after login follow you need to create an index pattern and see the report in the `Discover` page. 