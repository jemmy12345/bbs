#!/bin/bash
# 本地启动前端 (Vue CLI 开发服务器), 接口代理到 localhost:9082
# 用法: scripts/dev-frontend.sh start|stop|restart|status
#
# 可选环境变量:
#   FRONTEND_PORT  前端端口 (默认 8080, vue.config 默认 80 需要 root)
#   LOG_DIR        日志目录 (默认 runtime/logs)

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="$ROOT/frontend"
FRONTEND_PORT="${FRONTEND_PORT:-80}"
LOG_DIR="${LOG_DIR:-$ROOT/runtime/logs}"
RUN_DIR="$ROOT/.run"
PID_FILE="$RUN_DIR/frontend.pid"
LOG_FILE="$LOG_DIR/frontend.log"

info() { echo "[frontend] $*"; }
fail() { echo "[frontend] ERROR: $*" >&2; exit 1; }

is_running() {
  [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

start() {
  if is_running; then info "已在运行 (PID $(cat "$PID_FILE"))"; return; fi
  command -v npm >/dev/null || fail "找不到 npm"
  nc -z localhost "$FRONTEND_PORT" 2>/dev/null && fail "端口 $FRONTEND_PORT 已被占用 (可设置 FRONTEND_PORT)"
  if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    info "首次运行, 安装依赖 (npm install)..."
    (cd "$FRONTEND_DIR" && npm install) || fail "npm install 失败"
  fi
  mkdir -p "$RUN_DIR" "$LOG_DIR"
  info "启动前端, 日志: $LOG_FILE"
  (cd "$FRONTEND_DIR" && port="$FRONTEND_PORT" nohup npm run dev > "$LOG_FILE" 2>&1 &)
  sleep 1
  pgrep -f "vue-cli-service serve" | tail -1 > "$PID_FILE"
  info "等待端口 $FRONTEND_PORT (最多 120 秒)..."
  for _ in $(seq 1 120); do
    if nc -z localhost "$FRONTEND_PORT" 2>/dev/null; then
      info "启动成功: http://localhost:$FRONTEND_PORT"
      return
    fi
    is_running || fail "进程已退出, 查看日志: $LOG_FILE"
    sleep 1
  done
  fail "超时未监听 $FRONTEND_PORT, 查看日志: $LOG_FILE"
}

stop() {
  if is_running; then
    pkill -P "$(cat "$PID_FILE")" 2>/dev/null
    kill "$(cat "$PID_FILE")" 2>/dev/null && info "已停止"
  else
    pkill -f "vue-cli-service serve" 2>/dev/null && info "已停止残留进程" || info "未在运行"
  fi
  rm -f "$PID_FILE"
}

status() {
  if is_running; then
    info "运行中 (PID $(cat "$PID_FILE")), 端口 $FRONTEND_PORT $(nc -z localhost "$FRONTEND_PORT" 2>/dev/null && echo 已监听 || echo 未监听)"
  else
    info "未运行"
  fi
}

case "$1" in
  start)   start ;;
  stop)    stop ;;
  restart) stop; sleep 2; start ;;
  status)  status ;;
  *) echo "用法: $0 start|stop|restart|status"; exit 1 ;;
esac
