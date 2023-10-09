#!/usr/bin/env bash

kubectl create namespace vault
helm repo add hashicorp https://helm.releases.hashicorp.com
helm install vault hashicorp/vault \
     -n vault \
     --version 0.25.0

sleep 60

kubectl exec vault-0 -n vault \
   -- vault operator init \
   -key-shares=1 \
   -key-threshold=1 \
   -format=json > cluster-keys.json

VAULT_UNSEAL_KEY=$(jq -r ".unseal_keys_hex[]" cluster-keys.json)

kubectl exec vault-0 -n vault \
        -- vault operator unseal $VAULT_UNSEAL_KEY

VAULT_ROOT_TOKEN=$(jq -r ".root_token" cluster-keys.json)

kubectl create sa token-reviewer -n vault

kubectl create clusterrolebinding vault-client-auth-delegator \
        --clusterrole=system:auth-delegator \
        --serviceaccount=vault:token-reviewer

kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: token-reviewer-jwt
  namespace: vault
  annotations:
    kubernetes.io/service-account.name: token-reviewer
type: kubernetes.io/service-account-token
EOF

JWT_REVIEW_TOKEN=$(kubectl get secrets token-reviewer-jwt -n vault -o jsonpath='{.data.token}' | base64 -d)

kubectl exec vault-0 -n vault -- vault login "${VAULT_ROOT_TOKEN}"

kubectl exec vault-0 -n vault -- vault secrets enable -version=2 -path="database" kv

kubectl exec vault-0 -n vault -- vault kv put database/dbpassword password=${DC_APP}pwd

kubectl exec vault-0 -n vault -- vault auth enable kubernetes

kubectl exec vault-0 -n vault -- vault write auth/kubernetes/config \
        token_reviewer_jwt="${JWT_REVIEW_TOKEN}" \
        kubernetes_host=https://kubernetes.default.svc.cluster.local \
        kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt

kubectl exec vault-0 -n vault -- vault write auth/kubernetes/role/dbpassword \
        bound_service_account_names="*" \
        bound_service_account_namespaces="*" \
        policies=db-password \
        ttl=1h

kubectl exec vault-0 -n vault -- echo 'path "database/dbpassword" {capabilities = ["list","read"]}' | vault policy-write db-password -
