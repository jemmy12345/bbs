#!/bin/bash
# 本地启动后端 (SmartBbsApplication)
# 用法: scripts/dev-backend.sh start|stop|restart|status|build
#
# 可选环境变量:
#   JAVA8_HOME   JDK 8 路径 (默认 temurin-8)
#   REDIS_CONTAINER  本地 Redis 容器名 (默认 smart-bbs-redis)
#   LOG_DIR      日志目录 (默认 runtime/logs)
#   REBUILD=1    start 前强制重新打包

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BACKEND_DIR="$ROOT/backend/bbs-admin"
JAR="$BACKEND_DIR/target/bbs-admin.jar"
JAVA8_HOME="${JAVA8_HOME:-/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home}"
JAVA="$JAVA8_HOME/bin/java"
REDIS_CONTAINER="${REDIS_CONTAINER:-smart-bbs-redis}"
LOG_DIR="${LOG_DIR:-$ROOT/runtime/logs}"
RUN_DIR="$ROOT/.run"
PID_FILE="$RUN_DIR/backend.pid"
PORT=9082
CFG_DIR="$RUN_DIR/backend-cfg"

info() { echo "[backend] $*"; }
fail() { echo "[backend] ERROR: $*" >&2; exit 1; }

check_prereqs() {
  [ -x "$JAVA" ] || fail "找不到 JDK 8: $JAVA (请设置 JAVA8_HOME)"
  command -v mvn >/dev/null || fail "找不到 mvn"
  command -v docker >/dev/null || fail "找不到 docker (Redis 需要)"
  nc -z localhost 3306 2>/dev/null || info "警告: 3306 未监听, MySQL 可能未启动"
}

ensure_redis() {
  if docker exec "$REDIS_CONTAINER" redis-cli ping 2>/dev/null | grep -q PONG; then
    info "Redis 已就绪"
    return
  fi
  if docker ps -a --format '{{.Names}}' | grep -qx "$REDIS_CONTAINER"; then
    info "启动 Redis 容器 $REDIS_CONTAINER"
    docker start "$REDIS_CONTAINER" >/dev/null
  else
    info "创建 Redis 容器 $REDIS_CONTAINER"
    docker run -d --name "$REDIS_CONTAINER" -p 6379:6379 redis:7-alpine >/dev/null
  fi
  sleep 2
  docker exec "$REDIS_CONTAINER" redis-cli ping >/dev/null 2>&1 || fail "Redis 启动失败"
}

build() {
  info "打包 bbs-admin 及依赖模块 (JDK 8)..."
  (cd "$ROOT" && JAVA_HOME="$JAVA8_HOME" mvn -q -pl backend/bbs-admin -am -DskipTests package) \
    || fail "Maven 打包失败"
}

# 生成日志路径改写后的 logback 配置, 通过 loader.path 优先加载
# (原 logback.xml 写死了服务器路径 /data/cms/server/logs)
prepare_logback() {
  mkdir -p "$CFG_DIR" "$LOG_DIR"
  sed "s|/data/cms/server/logs|$LOG_DIR|" "$BACKEND_DIR/src/main/resources/logback.xml" > "$CFG_DIR/logback.xml"
}

is_running() {
  [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

start() {
  if is_running; then info "已在运行 (PID $(cat "$PID_FILE"))"; return; fi
  check_prereqs
  ensure_redis
  if [ ! -f "$JAR" ] || [ "${REBUILD:-0}" = "1" ]; then build; fi
  prepare_logback
  mkdir -p "$RUN_DIR"
  info "启动后端, 日志: $LOG_DIR/backend-stdout.log"
  (cd "$BACKEND_DIR" && nohup "$JAVA" -Dloader.path="$CFG_DIR" -cp "$JAR" \
      org.springframework.boot.loader.PropertiesLauncher \
      > "$LOG_DIR/backend-stdout.log" 2>&1 &)
  sleep 1
  pgrep -f "PropertiesLauncher" | tail -1 > "$PID_FILE"
  info "等待端口 $PORT (最多 180 秒)..."
  for _ in $(seq 1 180); do
    if nc -z localhost "$PORT" 2>/dev/null; then
      info "启动成功: http://localhost:$PORT"
      return
    fi
    is_running || fail "进程已退出, 查看日志: $LOG_DIR/backend-stdout.log"
    sleep 1
  done
  fail "超时未监听 $PORT, 查看日志: $LOG_DIR/backend-stdout.log"
}

stop() {
  if is_running; then
    kill "$(cat "$PID_FILE")" && info "已停止 (PID $(cat "$PID_FILE"))"
  else
    pkill -f "PropertiesLauncher" 2>/dev/null && info "已停止残留进程" || info "未在运行"
  fi
  rm -f "$PID_FILE"
}

status() {
  if is_running; then
    info "运行中 (PID $(cat "$PID_FILE")), 端口 $PORT $(nc -z localhost $PORT 2>/dev/null && echo 已监听 || echo 未监听)"
  else
    info "未运行"
  fi
}

case "$1" in
  start)   start ;;
  stop)    stop ;;
  restart) stop; sleep 2; start ;;
  status)  status ;;
  build)   build ;;
  *) echo "用法: $0 start|stop|restart|status|build"; exit 1 ;;
esac
