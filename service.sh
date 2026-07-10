#!/bin/bash
# 微服务管理脚本 - 启动 / 停止 / 重启 / 状态查看
# 用法: ./service.sh {start|stop|restart|status} [service_name ...]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_DIR="$SCRIPT_DIR/.pids"

# 可运行的 Spring Boot 服务及其端口
declare -A SERVICES=(
    ["user-service"]="28082"
    ["device-service"]="28084"
    ["auth-service"]="28081"
    ["admin-service"]="28085"
    ["gateway-service"]="28888"
)

# 服务别名 -> 全名
declare -A ALIAS=(
    ["user"]="user-service"
    ["device"]="device-service"
    ["auth"]="auth-service"
    ["admin"]="admin-service"
    ["gtw"]="gateway-service"
    ["all"]="__all__"
)

# 启动顺序（依赖关系决定）
START_ORDER=("user-service" "device-service" "auth-service" "admin-service" "gateway-service")
# 停止顺序（与启动相反）
STOP_ORDER=("gateway-service" "admin-service" "auth-service" "device-service" "user-service")

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ---------- 工具函数 ----------

init_dirs() {
    mkdir -p "$PID_DIR"
}

pid_file() {
    echo "$PID_DIR/$1.pid"
}

is_running() {
    local svc="$1"
    local pid
    pid=$(cat "$(pid_file "$svc")" 2>/dev/null || echo "")
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
        return 0
    fi
    # 清理残留 pid 文件
    rm -f "$(pid_file "$svc")"
    return 1
}

get_pid() {
    cat "$(pid_file "$1")" 2>/dev/null || echo ""
}

# ---------- 核心操作 ----------

do_start() {
    local svc="$1"
    if is_running "$svc"; then
        echo -e "  ${YELLOW}● $svc${NC} 已在运行 (PID $(get_pid "$svc"))"
        return 0
    fi

    local project_dir="$SCRIPT_DIR/$svc"
    if [[ ! -d "$project_dir" ]]; then
        echo -e "  ${RED}✗ $svc${NC} 目录不存在: $project_dir"
        return 1
    fi

    # 先构建 jar（跳过测试），再用 java -jar 直接运行（单进程，kill 彻底）
    echo -n "  构建 $svc ... "
    (cd "$project_dir" && mvn package -DskipTests -q 2>/dev/null)
    if [[ $? -ne 0 ]]; then
        echo -e "${RED}构建失败${NC}"
        return 1
    fi
    echo -e "${GREEN}完成${NC}"

    # 查找构建好的 jar
    local jar_file
    jar_file=$(find "$project_dir/target" -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
    if [[ -z "$jar_file" ]]; then
        echo -e "  ${RED}✗ $svc${NC} 未找到 jar 文件"
        return 1
    fi

    # 直接用 java -jar 启动（单进程，PID 追踪准确）
    (
        cd "$project_dir"
        nohup java -jar "$jar_file" &>/dev/null &
        echo $!
    ) > "$(pid_file "$svc")"

    local pid
    pid=$(get_pid "$svc")
    echo -e "  ${GREEN}● $svc${NC} 已启动  PID=$pid"
}

do_stop() {
    local svc="$1"
    if ! is_running "$svc"; then
        echo -e "  ${YELLOW}● $svc${NC} 未运行"
        return 0
    fi

    local pid
    pid=$(get_pid "$svc")
    echo -n "  停止 $svc (PID $pid) ... "

    # 先发 SIGTERM，等待优雅关闭
    kill "$pid" 2>/dev/null || true
    local waited=0
    while kill -0 "$pid" 2>/dev/null; do
        if (( waited >= 30 )); then
            # 30 秒还没停，强制 kill
            kill -9 "$pid" 2>/dev/null || true
            break
        fi
        sleep 1
        waited=$((waited + 1))
    done
    rm -f "$(pid_file "$svc")"
    echo -e "${GREEN}✓${NC}"
}

do_restart() {
    local svc="$1"
    echo -e "  ${CYAN}重启 $svc${NC}"
    do_stop "$svc"
    do_start "$svc"
}

do_status_one() {
    local svc="$1"
    local port="${SERVICES[$svc]}"
    if is_running "$svc"; then
        echo -e "  ${GREEN}● $svc${NC}  运行中  PID=$(get_pid "$svc")  端口=$port"
    else
        echo -e "  ${RED}○ $svc${NC}  未运行  端口=$port"
    fi
}

resolve_services() {
    # 将输入（别名或全名）展开为全名列表
    local input_services=("$@")
    if (( ${#input_services[@]} == 0 )); then
        echo "${START_ORDER[@]}"
        return
    fi
    local result=()
    for svc in "${input_services[@]}"; do
        # 检查别名
        if [[ -n "${ALIAS[$svc]+_}" ]]; then
            local resolved="${ALIAS[$svc]}"
            if [[ "$resolved" == "__all__" ]]; then
                echo "${START_ORDER[@]}"
                return
            fi
            result+=("$resolved")
        elif [[ -n "${SERVICES[$svc]+_}" ]]; then
            result+=("$svc")
        else
            echo -e "${RED}未知服务: $svc${NC}" >&2
            echo -e "可用服务: ${!SERVICES[*]}" >&2
            echo -e "别名:     ${!ALIAS[*]}" >&2
            exit 1
        fi
    done
    echo "${result[@]}"
}

# 按启动顺序过滤出需要操作的服务
order_filter() {
    local order_ref=$1
    shift
    local target_set=("$@")
    local result=()

    if [[ "$order_ref" == "start" ]]; then
        for svc in "${START_ORDER[@]}"; do
            for t in "${target_set[@]}"; do
                [[ "$svc" == "$t" ]] && result+=("$svc")
            done
        done
    else
        for svc in "${STOP_ORDER[@]}"; do
            for t in "${target_set[@]}"; do
                [[ "$svc" == "$t" ]] && result+=("$svc")
            done
        done
    fi
    echo "${result[@]}"
}

# ---------- 主入口 ----------

usage() {
    cat <<EOF
微服务管理脚本

用法: $(basename "$0") <command> [service ...]

命令:
  start    [svc ...]   启动服务（按依赖顺序）
  stop     [svc ...]   停止服务（按依赖逆序）
  restart  [svc ...]   重启服务
  status   [svc ...]   查看服务状态

不指定服务时，操作全部服务。

服务别名:
  user   = user-service     (28082)
  device = device-service   (28084)
  auth   = auth-service     (28081)
  admin  = admin-service    (28085)
  gtw    = gateway-service  (28888)
  all    = 全部服务

示例:
  $(basename "$0") start                      # 启动全部
  $(basename "$0") start user                 # 启动 user-service
  $(basename "$0") start user device          # 同时启动 user + device
  $(basename "$0") stop gtw                   # 停止 gateway-service
  $(basename "$0") restart auth               # 重启 auth-service
  $(basename "$0") status                     # 查看全部状态
  $(basename "$0") status user auth gtw       # 查看指定服务状态
EOF
}

main() {
    if (( $# < 1 )); then
        usage
        exit 1
    fi

    local command="$1"
    shift

    init_dirs

    case "$command" in
        start)
            local targets
            targets=($(resolve_services "$@"))
            local ordered
            ordered=($(order_filter "start" "${targets[@]}"))
            echo -e "${CYAN}=== 启动服务 ===${NC}"
            for svc in "${ordered[@]}"; do
                do_start "$svc"
            done
            echo ""
            echo -e "${CYAN}=== 完成 ===${NC}"
            ;;
        stop)
            local targets
            targets=($(resolve_services "$@"))
            local ordered
            ordered=($(order_filter "stop" "${targets[@]}"))
            echo -e "${CYAN}=== 停止服务 ===${NC}"
            for svc in "${ordered[@]}"; do
                do_stop "$svc"
            done
            echo ""
            echo -e "${CYAN}=== 完成 ===${NC}"
            ;;
        restart)
            local targets
            targets=($(resolve_services "$@"))
            local ordered_stop
            ordered_stop=($(order_filter "stop" "${targets[@]}"))
            local ordered_start
            ordered_start=($(order_filter "start" "${targets[@]}"))
            echo -e "${CYAN}=== 重启服务 ===${NC}"
            for svc in "${ordered_stop[@]}"; do
                do_stop "$svc"
            done
            for svc in "${ordered_start[@]}"; do
                do_start "$svc"
            done
            echo ""
            echo -e "${CYAN}=== 完成 ===${NC}"
            ;;
        status)
            local targets
            targets=($(resolve_services "$@"))
            echo -e "${CYAN}=== 服务状态 ===${NC}"
            for svc in "${targets[@]}"; do
                do_status_one "$svc"
            done
            ;;
        *)
            echo -e "${RED}未知命令: $command${NC}" >&2
            usage
            exit 1
            ;;
    esac
}

main "$@"
