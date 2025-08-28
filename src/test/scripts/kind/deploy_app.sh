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
    SED_COMMAND="sed -i ''"
  else
    SED_COMMAND="sed -i"
  fi
  
  ${SED_COMMAND} "s/\${DC_APP}/${DC_APP}/g" ${TMP_DIR}/cluster.yaml
  ${SED_COMMAND} "s/\${NAMESPACE}/atlassian/g" ${TMP_DIR}/cluster.yaml
  
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
    
    # Get the primary pod name
    PRIMARY_POD=$(kubectl get pods -n atlassian -l cnpg.io/cluster=${DC_APP}-db,role=primary -o jsonpath='{.items[0].metadata.name}')
    
    # Copy and execute the initialization script
    kubectl cp ${DB_INIT_SCRIPT_FILE} atlassian/${PRIMARY_POD}:/tmp/db-init-script.sql -c postgres
    kubectl exec -n atlassian ${PRIMARY_POD} -c postgres -- psql -U ${DC_APP} -d ${DC_APP} -f /tmp/db-init-script.sql
  fi
  
  # Execute default initialization script
  echo "[INFO]: Executing default database initialization"
  TMP_INIT_DIR=$(mktemp -d)
  cp src/test/infrastructure/cloudnativepg/init-scripts/init-db.sql ${TMP_INIT_DIR}/init-db.sql
  ${SED_COMMAND} "s/\${DC_APP}/${DC_APP}/g" ${TMP_INIT_DIR}/init-db.sql
  
  PRIMARY_POD=$(kubectl get pods -n atlassian -l cnpg.io/cluster=${DC_APP}-db,role=primary -o jsonpath='{.items[0].metadata.name}')
  kubectl cp ${TMP_INIT_DIR}/init-db.sql atlassian/${PRIMARY_POD}:/tmp/init-db.sql -c postgres
  kubectl exec -n atlassian ${PRIMARY_POD} -c postgres -- psql -U ${DC_APP} -d ${DC_APP} -f /tmp/init-db.sql
  
  echo "[INFO]: PostgreSQL cluster ${DC_APP}-db is ready"
}

# Rest of the file remains unchanged...