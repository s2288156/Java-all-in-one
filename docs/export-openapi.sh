#!/bin/bash
# 导出各服务的 OpenAPI 文档（Apifox 可直接导入）

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/openapi"

mkdir -p "$OUTPUT_DIR"

declare -A SERVICES=(
    ["auth-service"]="28081"
    ["user-service"]="28082"
    ["device-service"]="28084"
    ["admin-service"]="28085"
)

echo "=== 导出 OpenAPI 文档 ==="

for service in "${!SERVICES[@]}"; do
    port="${SERVICES[$service]}"
    url="http://127.0.0.1:${port}/v3/api-docs"
    output="$OUTPUT_DIR/${service}.json"

    echo -n "导出 $service ... "
    if curl -sf "$url" -o "$output" 2>/dev/null; then
        echo "✓ -> $output"
    else
        echo "✗ (服务未启动或端口 ${port} 不可达)"
    fi
done

echo ""
echo "=== 导出完成 ==="
echo "文件位置: $OUTPUT_DIR/"
echo ""
echo "Apifox 导入方式:"
echo "  1. 打开 Apifox -> 项目设置 -> 导入数据"
echo "  2. 选择 OpenAPI/Swagger 格式"
echo "  3. 选择对应的 JSON 文件导入"
