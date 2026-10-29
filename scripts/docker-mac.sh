#!/bin/bash
# Smart BBS 一键部署脚本（macOS，Intel 芯片与 Apple 芯片均可）
#
# 首次运行会自动完成：
#   1. 未安装 Docker 时，下载并安装 Docker Desktop（按 macOS 版本选择可用的版本；
#      macOS 11 Big Sur 使用 4.24.2，这是最后一个支持 Big Sur 的版本）
#   2. 启动 Docker Desktop（首次启动需在弹出的窗口中点“Accept”同意许可协议）
#   3. 构建并启动 MySQL 8、Redis 7、后端、前端四个容器，首次启动时自动导入 sql/bbsdb.sql
#
# 用法：bash scripts/docker-mac.sh [up|down|restart|status|logs|reset]
#   up       安装环境并启动全部服务（默认）
#   down     停止并删除容器（保留数据库数据）
#   restart  重新构建镜像并重启（代码更新后使用）
#   status   查看容器状态
#   logs     查看日志，可追加服务名：logs backend
#   reset    删除容器和全部数据卷，下次 up 时重新导入数据库
#
# 可选环境变量：
#   WEB_PORT=80  BACKEND_PORT=9082  MYSQL_PORT=3307  MYSQL_ROOT_PASSWORD=smartbbs123
#   CN_MIRROR=1         构建时使用国内镜像加速 Maven、npm 下载
#   DOCKER_DMG_URL=...  指定 Docker Desktop 安装包地址（自动选择的版本装不上时使用）

set -eu

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="$ROOT/deploy/docker/docker-compose.yml"
export WEB_PORT="${WEB_PORT:-80}"
export BACKEND_PORT="${BACKEND_PORT:-9082}"
export MYSQL_PORT="${MYSQL_PORT:-3307}"
export MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-smartbbs123}"
export CN_MIRROR="${CN_MIRROR:-0}"
export PATH="/usr/local/bin:$HOME/.docker/bin:/Applications/Docker.app/Contents/Resources/bin:$PATH"

info() { printf '\033[32m[smart-bbs]\033[0m %s\n' "$*"; }
warn() { printf '\033[33m[smart-bbs]\033[0m %s\n' "$*"; }
fail() { printf '\033[31m[smart-bbs] ERROR:\033[0m %s\n' "$*" >&2; exit 1; }

MACOS_MAJOR=0
check_platform() {
  [ "$(uname -s)" = "Darwin" ] || fail "本脚本仅支持 macOS"
  local ver
  ver="$(sw_vers -productVersion)"
  MACOS_MAJOR="${ver%%.*}"
  case "$(uname -m)" in
    x86_64) info "检测到 Intel 芯片 Mac（macOS ${ver}）" ;;
    arm64)  info "检测到 Apple 芯片 Mac（macOS ${ver}）" ;;
    *)      fail "不支持的架构：$(uname -m)" ;;
  esac
  [ "$MACOS_MAJOR" -ge 11 ] || fail "Docker Desktop 需要 macOS 11 及以上版本，当前为 $ver"
  [ -f "$ROOT/sql/bbsdb.sql" ] || fail "缺少数据库初始化文件 sql/bbsdb.sql"
}

docker_ready() { command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; }

wait_docker() {
  local i
  for i in $(seq 1 150); do
    docker_ready && return 0
    sleep 2
  done
  return 1
}

docker_dmg_url() {
  if [ -n "${DOCKER_DMG_URL:-}" ]; then
    echo "$DOCKER_DMG_URL"; return
  fi
  local arch=amd64
  [ "$(uname -m)" = "arm64" ] && arch=arm64
  if [ "$MACOS_MAJOR" -le 11 ]; then
    # Docker Desktop 4.24.2：最后一个支持 macOS 11 Big Sur 的版本
    echo "https://desktop.docker.com/mac/main/${arch}/124339/Docker.dmg"
  else
    echo "https://desktop.docker.com/mac/main/${arch}/Docker.dmg"
  fi
}

install_docker_desktop() {
  local url dmg mnt
  url="$(docker_dmg_url)"
  dmg="$HOME/Downloads/Docker-smart-bbs.dmg"
  info "下载 Docker Desktop 安装包（约 600MB）：$url"
  curl -fL --retry 3 -C - -o "$dmg" "$url" || fail "下载失败，可手动下载后双击安装：$url"
  info "安装 Docker Desktop 到“应用程序”..."
  mnt="$(mktemp -d /tmp/docker-dmg.XXXXXX)"
  hdiutil attach -nobrowse -quiet -mountpoint "$mnt" "$dmg" || fail "安装包无法打开，可删除 $dmg 后重试"
  if ! cp -R "$mnt/Docker.app" /Applications/ 2>/dev/null; then
    warn "需要管理员权限复制到“应用程序”，请输入开机密码"
    sudo cp -R "$mnt/Docker.app" /Applications/
  fi
  hdiutil detach -quiet "$mnt" || true
  rm -f "$dmg"
  info "Docker Desktop 安装完成"
}

ensure_docker() {
  if docker_ready; then
    info "Docker 已在运行"
    return
  fi
  [ -d "/Applications/Docker.app" ] || install_docker_desktop
  info "启动 Docker Desktop……首次启动请在弹出的窗口中点“Accept”同意许可协议，然后等待鲸鱼图标停止闪动"
  open -a Docker
  wait_docker || fail "Docker Desktop 启动超时（5 分钟）。请手动打开“应用程序 → Docker”，确认已启动后重新运行本脚本"
  info "Docker 已就绪（$(docker version --format '{{.Server.Version}}' 2>/dev/null)）"
}

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose -f "$COMPOSE_FILE" "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose -f "$COMPOSE_FILE" "$@"
  else
    fail "未找到 docker compose，请升级 Docker Desktop"
  fi
}

check_ports() {
  # 本项目容器已在运行时，端口由自身占用，不再检查
  [ -n "$(compose ps -q 2>/dev/null)" ] && return 0
  local p
  for p in "$WEB_PORT" "$BACKEND_PORT" "$MYSQL_PORT"; do
    if lsof -nP -iTCP:"$p" -sTCP:LISTEN >/dev/null 2>&1; then
      fail "端口 $p 已被占用，可换端口运行，例如：WEB_PORT=8080 BACKEND_PORT=19082 MYSQL_PORT=13307 bash $0"
    fi
  done
}

wait_backend() {
  local i
  info "等待后端启动（首次启动需导入数据库，约 1–3 分钟）..."
  for i in $(seq 1 180); do
    if curl -s -o /dev/null "http://localhost:${BACKEND_PORT}/captchaImage"; then
      return 0
    fi
    sleep 2
  done
  return 1
}

up() {
  check_platform
  ensure_docker
  check_ports
  if [ "$CN_MIRROR" = "1" ]; then info "已启用国内镜像加速（Maven、npm）"; fi
  info "构建镜像并启动服务（首次构建需下载依赖，约 5–15 分钟）..."
  mkdir -p "$ROOT/runtime/upload"
  compose up -d --build
  if wait_backend; then
    info "部署完成"
    echo
    echo "  前台地址：  http://localhost:${WEB_PORT}/bbs/"
    echo "  后端接口：  http://localhost:${BACKEND_PORT}"
    echo "  MySQL：     localhost:${MYSQL_PORT}  用户 root  密码 ${MYSQL_ROOT_PASSWORD}  库 bbsdb"
    echo "  查看日志：  bash $0 logs backend"
    echo
  else
    fail "后端启动超时，请执行 bash $0 logs backend 查看原因"
  fi
}

case "${1:-up}" in
  up)      up ;;
  down)    compose down ;;
  restart) check_platform; ensure_docker; compose up -d --build --force-recreate; wait_backend && info "已重启" ;;
  status)  compose ps ;;
  logs)    shift; compose logs -f --tail=200 "$@" ;;
  reset)
    printf '将删除数据库和 Redis 的全部数据，确认？[y/N] '
    read -r ans
    case "$ans" in y|Y) ;; *) info "已取消"; exit 0 ;; esac
    compose down -v
    info "已清空，执行 bash $0 up 重新部署" ;;
  *) echo "用法: bash $0 [up|down|restart|status|logs|reset]"; exit 1 ;;
esac
