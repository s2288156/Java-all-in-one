#!/bin/bash
# Upload all Nacos configs from nacos/ directory to Nacos server
# Usage: ./nacos/upload-configs.sh [nacos-addr] [namespace]

NACOS_ADDR="${1:-127.0.0.1:8848}"
NAMESPACE="${2:-public}"
GROUP="DEFAULT_GROUP"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

for f in "$SCRIPT_DIR"/*.yml; do
  data_id=$(basename "$f")
  echo "Uploading $data_id ..."
  curl -s -X POST "http://$NACOS_ADDR/nacos/v1/cs/configs" \
    -d "dataId=$data_id&group=$GROUP&tenant=$NAMESPACE&type=yaml" \
    --data-urlencode "content@$f"
  echo ""
done
echo "Done."
