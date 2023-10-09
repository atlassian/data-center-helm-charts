#!/usr/bin/env bash
NAMESPACE=$1
export ATLASSIAN_NAMESPACE=${NAMESPACE:-atlassian}

deploy_vault() {
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
}

unseal_vault() {
  echo "[INFO]: Getting keys"

  kubectl exec vault-0 -n vault \
     -- vault operator init \
     -key-shares=1 \
     -key-threshold=1 \
     -format=json > cluster-keys.json

  VAULT_UNSEAL_KEY=$(jq -r ".unseal_keys_hex[]" cluster-keys.json)

  echo "[INFO]: Unsealing Vault"

  kubectl exec vault-0 -n vault \
          -- vault operator unseal "${VAULT_UNSEAL_KEY}"
}

create_token_review_sa() {
  echo "[INFO]: Creating token-reviewer service account"

  kubectl create sa token-reviewer -n vault

  echo "[INFO]: Creating token-reviewer vault-client-auth-delegator ClusterRoleBinding"

  kubectl create clusterrolebinding vault-client-auth-delegator \
          --clusterrole=system:auth-delegator \
          --serviceaccount=vault:token-reviewer

  echo "[INFO]: Creating token-reviewer-jwt secret"

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
}

vault_log_in() {
  echo "[INFO]: Logging in"
  VAULT_ROOT_TOKEN=$(jq -r ".root_token" cluster-keys.json)
  kubectl exec vault-0 -n vault -- vault login "${VAULT_ROOT_TOKEN}"
}

create_secret() {
  echo "[INFO]: Enable kv2 secret engine at database path"
  kubectl exec vault-0 -n vault -- vault secrets enable -version=2 -path="database" kv
  echo "[INFO]: Writing dbpassword secret"
  kubectl exec vault-0 -n vault -- vault kv put database/dbpassword password=${DC_APP}pwd
}

enable_k8s_auth_method() {
  echo "[INFO]: Getting token-reviewer jwt token"
  echo "[INFO]: Enabling Kubernetes auth method"
  kubectl exec vault-0 -n vault -- vault auth enable kubernetes
  echo "[INFO]: Configuring Kubernetes auth method"
}

configure_k8s_auth_method() {
  export JWT_REVIEW_TOKEN=$(kubectl get secrets token-reviewer-jwt -n vault -o jsonpath='{.data.token}' | base64 -d)
  kubectl exec vault-0 -n vault -- vault write auth/kubernetes/config \
          token_reviewer_jwt="${JWT_REVIEW_TOKEN}" \
          kubernetes_host=https://kubernetes.default.svc.cluster.local \
          kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
}

create_role() {
  echo "[INFO]: Creating dbpassword role"
  kubectl exec vault-0 -n vault -- vault write auth/kubernetes/role/dbpassword \
          bound_service_account_names="*" \
          bound_service_account_namespaces="${ATLASSIAN_NAMESPACE}" \
          policies=dbpassword \
          ttl=1h
}

create_policy() {
  echo "[INFO]: Creating dbpassword policy"
  kubectl exec vault-0 -n vault -- sh -c "echo 'path \"database/data/dbpassword\" {capabilities = [\"list\",\"read\"]}' | vault policy write dbpassword -"

}

test_k8s_auth() {
  echo "[INFO]: Testing Kubernetes role"
  echo "[INFO]: Creating a test service account ${DC_APP}-test in ${ATLASSIAN_NAMESPACE} namespace"
  kubectl create sa ${DC_APP}-test -n ${ATLASSIAN_NAMESPACE}
  echo "[INFO]: Creating ${DC_APP}-jwt secrtet in ${ATLASSIAN_NAMESPACE} namespace"

  kubectl apply -f - <<EOF
  apiVersion: v1
  kind: Secret
  metadata:
    name: ${DC_APP}-test-jwt-token
    namespace: ${ATLASSIAN_NAMESPACE}
    annotations:
      kubernetes.io/service-account.name: ${DC_APP}-test
  type: kubernetes.io/service-account-token
EOF

  echo "[INFO]: Getting ${DC_APP}-test jwt token"
  JWT_TEST_TOKEN=$(kubectl get secrets ${DC_APP}-test-jwt-token -n ${ATLASSIAN_NAMESPACE} -o jsonpath='{.data.token}' | base64 -d)

  echo "[INFO]: Getting Vault token"
  kubectl run curl-token --restart=Never --image appropriate/curl -- -s -X "POST" "http://vault-internal.vault.svc.cluster.local:8200/v1/auth/kubernetes/login" -d "{\"role\": \"dbpassword\", \"jwt\": \"${JWT_TEST_TOKEN}\"}"
  kubectl wait --timeout=60s --for=jsonpath='{.status.phase}'=Succeeded pod/curl-token
  VAULT_TOKEN=$(kubectl logs curl-token | jq .auth.client_token | sed 's/"//g')

  if [ -z "${VAULT_TOKEN}" ]; then
    echo "[ERROR]: Can't get Vault token. Vault response was:"
    kubectl logs curl-token
    exit 1
  fi

  echo "[INFO]: Getting Vault secret"

  kubectl run curl-secret --restart=Never --image appropriate/curl -- -s --header "X-Vault-Token: ${VAULT_TOKEN}" http://vault-internal.vault.svc.cluster.local:8200/v1/database/data/dbpassword
  kubectl wait --timeout=60s --for=jsonpath='{.status.phase}'=Succeeded pod/curl-secret
  PASSWORD=$(kubectl logs curl-secret | jq .data.data.password | sed 's/"//g')

  if [ $PASSWORD != ${DC_APP}pwd ]; then
    echo "[ERROR]: Can't get password or passwords do not match. Response from Vault:"
    kubectl logs curl-secret
    exit 1
  fi
}

deploy_vault
unseal_vault
create_token_review_sa
vault_log_in
create_secret
enable_k8s_auth_method
configure_k8s_auth_method
create_role
create_policy
test_k8s_auth


