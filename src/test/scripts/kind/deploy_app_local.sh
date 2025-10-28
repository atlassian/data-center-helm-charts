#!/usr/bin/env bash

# Deploy CloudNativePG operator and PostgreSQL cluster
deploy_postgres() {
  echo "[INFO]: Installing CloudNativePG operator"
  
  # Add CloudNativePG Helm repository
  helm repo add cloudnative-pg https://cloudnative-pg.github.io/charts --force-update
  helm repo update
  
  # Install CloudNativePG operator
  echo "[INFO]: Installing CloudNativePG operator"
  helm upgrade --install cnpg-operator cloudnative-pg/cloudnative-pg \
       --values src/test/infrastructure/cloudnativepg/operator-values.yaml \
       --namespace cnpg-system \
       --create-namespace \
       --wait --timeout=300s
  
  # Wait for operator to be ready
  echo "[INFO]: Waiting for CloudNativePG operator to be ready"
  for i in {1..60}; do
    if kubectl get crd clusters.postgresql.cnpg.io >/dev/null 2>&1; then
      echo "[INFO]: CloudNativePG operator is ready"
      break
    fi
    echo "[INFO]: Waiting for CloudNativePG CRDs to be available... ($i/60)"
    sleep 5
  done
  
  # Create database credentials secret
  echo "[INFO]: Creating database credentials secret"
  kubectl create secret generic ${DC_APP}-db-credentials \
    --from-literal=username="${DC_APP}" \
    --from-literal=password="${DC_APP}pwd" \
    --namespace atlassian \
    --dry-run=client -o yaml | kubectl apply -f -
  
  # Create PostgreSQL cluster from template
  echo "[INFO]: Creating PostgreSQL cluster for ${DC_APP}"
  TMP_DIR=$(mktemp -d)
  cp src/test/infrastructure/cloudnativepg/cluster-template.yaml ${TMP_DIR}/cluster.yaml
  
  # Replace placeholders in cluster template
  if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "s/\${DC_APP}/${DC_APP}/g" ${TMP_DIR}/cluster.yaml
    sed -i '' "s/\${NAMESPACE}/atlassian/g" ${TMP_DIR}/cluster.yaml
  else
    sed -i "s/\${DC_APP}/${DC_APP}/g" ${TMP_DIR}/cluster.yaml
    sed -i "s/\${NAMESPACE}/atlassian/g" ${TMP_DIR}/cluster.yaml
  fi
  
  # Apply the cluster configuration
  kubectl apply -f ${TMP_DIR}/cluster.yaml
  
  # Wait for cluster to be ready
  echo "[INFO]: Waiting for PostgreSQL cluster to be ready"
  kubectl wait --for=condition=Ready cluster/${DC_APP}-db \
    --namespace atlassian --timeout=600s
  
  # Wait for primary pod to be ready
  echo "[INFO]: Waiting for PostgreSQL primary pod to be ready"
  kubectl wait --for=condition=Ready pod -l cnpg.io/cluster=${DC_APP}-db,role=primary \
    --namespace atlassian --timeout=300s
  
  # Execute custom initialization script if provided
  if [ -f "${DB_INIT_SCRIPT_FILE}" ]; then
    echo "[INFO]: DB init file '${DB_INIT_SCRIPT_FILE}' found. Initializing the database"
    PRIMARY_POD=$(kubectl get pods -n atlassian -l cnpg.io/cluster=${DC_APP}-db,role=primary -o jsonpath='{.items[0].metadata.name}')
    kubectl cp ${DB_INIT_SCRIPT_FILE} atlassian/${PRIMARY_POD}:/tmp/db-init-script.sql -c postgres
    kubectl exec -n atlassian ${PRIMARY_POD} -c postgres -- psql -U ${DC_APP} -d ${DC_APP} -f /tmp/db-init-script.sql
  fi
}

# Create secrets for local testing
create_secrets() {
  echo "[INFO]: Creating db, admin and license secrets"
  DC_APP_CAPITALIZED="$(echo ${DC_APP} | awk '{print toupper(substr($0,1,1)) tolower(substr($0,2))}')"

  kubectl create secret generic ${DC_APP}-db-credentials \
          --from-literal=username="${DC_APP}" \
          --from-literal=password="${DC_APP}pwd" \
          -n atlassian
  kubectl create secret generic ${DC_APP}-admin \
          --from-literal=username="admin" \
          --from-literal=password="admin" \
          --from-literal=displayName="${DC_APP_CAPITALIZED}" \
          --from-literal=emailAddress="${DC_APP}@example.com" \
          -n atlassian
}

# Deploy Jira with local test values
deploy_app() {
  helm repo add atlassian-data-center https://atlassian.github.io/data-center-helm-charts
  helm repo update
  helm dependency build ./src/main/charts/${DC_APP}

  # Create test values for Jira
  cat > /tmp/jira-test-values.yaml << 'EOF'
jira:
  clustering:
    enabled: true
  readinessProbe:
    failureThreshold: 100
  resources:
    container:
      requests:
        cpu: 20m
        memory: 1G
  additionalCertificates:
    secretList:
      - name: dev-certificates
        keys:
          - stg.crt
          - dev.crt
      - name: certificate-internal
        keys:
          - internal.crt
    initContainer:
      resources:
        requests:
          memory: 1Mi
          cpu: 1m
        limits:
          memory: 100Mi
          cpu: 1
database:
  type: postgres72
  url: jdbc:postgresql://jira-db-rw:5432/jira
  driver: org.postgresql.Driver
  credentials:
    secretName: jira-db-credentials
volumes:
  localHome:
    persistentVolumeClaim:
      create: true
      persistentVolumeClaimRetentionPolicy:
        whenDeleted: Delete
        whenScaled: Retain
  sharedHome:
    persistentVolumeClaim:
      create: false
      claimName: local-test-shared-home-pvc
ingress:
  create: true
  host: localhost
  https: false
  proxyConnectTimeout: 300
  proxyReadTimeout: 300
  proxySendTimeout: 300
monitoring:
  exposeJmxMetrics: true
  jmxExporter:
    version: 0.18.0
    mavenBaseUrl: https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent
    sha256: d3ba4ce8eccd3461463358c5e19f8becb99ed949f159c37c850e53b89b560f97
  serviceMonitor:
    create: true
atlassianAnalyticsAndSupport:
  analytics:
    enabled: false
  helmValues:
    enabled: true
EOF

  # Deploy Jira with test values
  helm upgrade --install ${DC_APP} ./src/main/charts/${DC_APP} \
               -f /tmp/jira-test-values.yaml \
               -n atlassian \
               --wait --timeout=360s \
               --debug
}

# Clean up any existing KinD cluster
clean_kind() {
  kind delete cluster --name atl-kind
}

# Create and configure KinD cluster
setup_kind() {
  # Create KinD cluster
  ./src/test/scripts/kind/create_kind.sh
  
  # Configure cluster
  ./src/test/scripts/kind/configure_kind.sh
  
  # Create local storage
  mkdir -p /tmp/local-test-shared-home
  
  # Create PV and PVC for shared home
  cat > /tmp/local-test-hostpath.yaml << 'EOF'
apiVersion: v1
kind: PersistentVolume
metadata:
  name: local-test-shared-home-pv
  labels:
    type: local
spec:
  storageClassName: manual
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteMany
  hostPath:
    path: "/tmp/local-test-shared-home"
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: local-test-shared-home-pvc
  namespace: atlassian
spec:
  storageClassName: manual
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 1Gi
EOF

  kubectl apply -f /tmp/local-test-hostpath.yaml
}

# Main deployment sequence
deploy_all() {
  export HOSTPATH_PV=true
  export DC_APP=jira
  
  clean_kind
  setup_kind
  deploy_postgres
  create_secrets
  deploy_app
}

# If script is run directly, execute deploy_all
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  deploy_all
fi