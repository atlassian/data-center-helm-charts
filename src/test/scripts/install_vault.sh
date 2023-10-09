#!/usr/bin/env bash

echo "[INFO]: Creating vault namespace"
kubectl create namespace vault
echo "[INFO]: Installing Vault helm chart"
helm repo add hashicorp https://helm.releases.hashicorp.com
helm install vault hashicorp/vault \
     -n vault \
     --version 0.25.0

echo "[INFO]: Waiting for Vault pod to be running"

sleep 10
kubectl wait --timeout=60s --for=jsonpath='{.status.phase}'=Running pod/vault-0 -n vault


echo "[INFO]: Getting keys"

kubectl exec vault-0 -n vault \
   -- vault operator init \
   -key-shares=1 \
   -key-threshold=1 \
   -format=json > cluster-keys.json

VAULT_UNSEAL_KEY=$(jq -r ".unseal_keys_hex[]" cluster-keys.json)

echo "[INFO]: Unsealing Vault"

kubectl exec vault-0 -n vault \
        -- vault operator unseal $VAULT_UNSEAL_KEY

VAULT_ROOT_TOKEN=$(jq -r ".root_token" cluster-keys.json)

echo "[INFO]: Creating token-reviewer service account"

kubectl create sa token-reviewer -n vault

echo "[INFO]: Creating token-reviewer vault-client-auth-delegator ClusterRoleBinding"

kubectl create clusterrolebinding vault-client-auth-delegator \
        --clusterrole=system:auth-delegator \
        --serviceaccount=vault:token-reviewer

echo "[INFO]: Creating token-reviewer-jwt secrtet"

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

echo "[INFO]: Getting token-reviewer jwt token"

JWT_REVIEW_TOKEN=$(kubectl get secrets token-reviewer-jwt -n vault -o jsonpath='{.data.token}' | base64 -d)

echo "[INFO]: Logging in"

kubectl exec vault-0 -n vault -- vault login "${VAULT_ROOT_TOKEN}"

echo "[INFO]: Enable kv2 secre engine at database path"

kubectl exec vault-0 -n vault -- vault secrets enable -version=2 -path="database" kv

echo "[INFO]: Writing dbpassword secret"

kubectl exec vault-0 -n vault -- vault kv put database/dbpassword password=${DC_APP}pwd

echo "[INFO]: Enabling Kubernetes auth method"

kubectl exec vault-0 -n vault -- vault auth enable kubernetes

echo "[INFO]: Configuring Kubernetes auth method"

kubectl exec vault-0 -n vault -- vault write auth/kubernetes/config \
        token_reviewer_jwt="${JWT_REVIEW_TOKEN}" \
        kubernetes_host=https://kubernetes.default.svc.cluster.local \
        kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt

echo "[INFO]: Creating dbpassword role"

kubectl exec vault-0 -n vault -- vault write auth/kubernetes/role/dbpassword \
        bound_service_account_names="*" \
        bound_service_account_namespaces="*" \
        policies=dbpassword \
        ttl=1h

echo "[INFO]: Creating dbpassword policy"
kubectl exec vault-0 -n vault -- sh -c "echo 'path \"database/dbpassword\" {capabilities = [\"list\",\"read\"]}' | vault policy write dbpassword -"


echo "[INFO]: Testing Kubernetes role"

kubectl run curl-token --restart=Never --image appropriate/curl -- -s -X "POST" "http://vault-internal.vault.svc.cluster.local:8200/v1/auth/kubernetes/login" -d "{\"role\": \"dbpassword\", \"jwt\": \"${JWT_REVIEW_TOKEN}\"}"

kubectl wait --timeout=60s --for=jsonpath='{.status.phase}'=Succeeded pod/curl-token

VAULT_TOKEN=$(kubectl logs curl-token | jq .auth.client_token | sed 's/"//g')

kubectl logs curl-token
echo ">>> ${VAULT_TOKEN}"

if [ -z "${VAULT_TOKEN}" ]; then
  echo "[ERROR]: Can't get Vault token. Vault response was:"
  kubectl logs curl-token
  exit 1
fi

kubectl run curl-secret --restart=Never --image appropriate/curl -- -s --header "X-Vault-Token: ${VAULT_TOKEN}" http://vault-internal.vault.svc.cluster.local:8200/v1/database/data/dbpassword

kubectl wait --timeout=60s --for=jsonpath='{.status.phase}'=Succeeded pod/curl-secret

kubectl logs curl-secret