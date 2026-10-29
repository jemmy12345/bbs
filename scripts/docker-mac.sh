#!/bin/bash
# Smart BBS 一键部署脚本（macOS，Intel 芯片优先，Apple 芯片同样可用）
#
# 首次运行会自动完成：
#   1. 安装 Homebrew（如未安装，会要求输入一次开机密码）
#   2. 安装 Docker 运行环境：已装 Docker Desktop 则直接使用；否则安装 Colima + Docker CLI（无需图形界面）
#   3. 构建并启动 MySQL 8、Redis 7、后端、前端四个容器，首次启动时自动导入 sql/bbsdb.sql
#
# 用法：scripts/docker-mac.sh [up|down|restart|status|logs|reset]
#   up       安装环境并启动全部服务（默认）
#   down     停止并删除容器（保留数据库数据）
#   restart  重新构建镜像并重启（代码更新后使用）
#   status   查看容器状态
#   logs     查看日志，可追加服务名：logs backend
#   reset    删除容器和全部数据卷，下次 up 时重新导入数据库
#
# 可选环境变量：
#   WEB_PORT=80  BACKEND_PORT=9082  MYSQL_PORT=3307  MYSQL_ROOT_PASSWORD=smartbbs123
#   CN_MIRROR=1  使用国内镜像加速 Homebrew、Maven、npm 下载
#   COLIMA_CPU=4 COLIMA_MEMORY=6     Colima 虚拟机的 CPU 核数和内存(GB)

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_FILE="$ROOT/deploy/docker/docker-compose.yml"
export WEB_PORT="${WEB_PORT:-80}"
export BACKEND_PORT="${BACKEND_PORT:-9082}"
export MYSQL_PORT="${MYSQL_PORT:-3307}"
export MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-smartbbs123}"
export CN_MIRROR="${CN_MIRROR:-0}"
COLIMA_CPU="${COLIMA_CPU:-4}"
COLIMA_MEMORY="${COLIMA_MEMORY:-6}"

info() { echo -e "\033[32m[smart-bbs]\033[0m $*"; }
warn() { echo -e "\033[33m[smart-bbs]\033[0m $*"; }
fail() { echo -e "\033[31m[smart-bbs] ERROR:\033[0m $*" >&2; exit 1; }

check_platform() {
  [ "$(uname -s)" = "Darwin" ] || fail "本脚本仅支持 macOS"
  case "$(uname -m)" in
    x86_64) info "检测到 Intel 芯片 Mac（macOS $(sw_vers -productVersion)）" ;;
    arm64)  info "检测到 Apple 芯片 Mac（macOS $(sw_vers -productVersion)），同样可以运行" ;;
    *)      fail "不支持的架构：$(uname -m)" ;;
  esac
  [ -f "$ROOT/sql/bbsdb.sql" ] || fail "缺少数据库初始化文件 sql/bbsdb.sql"
}

ensure_brew() {
  if [ "$CN_MIRROR" = "1" ]; then
    export HOMEBREW_API_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles/api"
    export HOMEBREW_BOTTLE_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles"
    export HOMEBREW_BREW_GIT_REMOTE="https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/brew.git"
  fi
  if ! command -v brew >/dev/null 2>&1; then
    for p in /usr/local/bin/brew /opt/homebrew/bin/brew; do
      [ -x "$p" ] && eval "$("$p" shellenv)" && break
    done
  fi
  if ! command -v brew >/dev/null 2>&1; then
    info "安装 Homebrew（需要输入开机密码）..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    for p in /usr/local/bin/brew /opt/homebrew/bin/brew; do
      [ -x "$p" ] && eval "$("$p" shellenv)" && break
    done
    command -v brew >/dev/null 2>&1 || fail "Homebrew 安装失败"
  fi
}

docker_ready() { docker info >/dev/null 2>&1; }

wait_docker() {
  for _ in $(seq 1 120); do
    docker_ready && return 0
    sleep 2
  done
  return 1
}

ensure_docker() {
  if command -v docker >/dev/null 2>&1 && docker_ready; then
    info "Docker 已在运行"
    return
  fi

  # 已安装 Docker Desktop：直接启动
  if [ -d "/Applications/Docker.app" ]; then
    info "启动 Docker Desktop（首次启动请在弹出的窗口中同意许可协议）..."
    open -a Docker
    wait_docker || fail "Docker Desktop 启动超时，请手动打开 Docker 后重新运行本脚本"
    info "Docker Desktop 已就绪"
    return
  fi

  # 未安装 Docker Desktop：用 Colima 提供 Docker 运行环境
  ensure_brew
  local pkgs=()
  command -v colima >/dev/null 2>&1 || pkgs+=(colima)
  command -v docker >/dev/null 2>&1 || pkgs+=(docker)
  brew list docker-compose >/dev/null 2>&1 || pkgs+=(docker-compose)
  brew list docker-buildx >/dev/null 2>&1 || pkgs+=(docker-buildx)
  if [ ${#pkgs[@]} -gt 0 ]; then
    info "安装 ${pkgs[*]} ..."
    brew install "${pkgs[@]}"
  fi

  # 让 docker 命令找到 compose / buildx 插件
  local plugin_dir
  plugin_dir="$(brew --prefix)/lib/docker/cli-plugins"
  mkdir -p "$HOME/.docker"
  if [ ! -f "$HOME/.docker/config.json" ]; then
    echo "{\"cliPluginsExtraDirs\": [\"$plugin_dir\"]}" > "$HOME/.docker/config.json"
  elif ! grep -q cliPluginsExtraDirs "$HOME/.docker/config.json"; then
    warn "请在 ~/.docker/config.json 中加入 \"cliPluginsExtraDirs\": [\"$plugin_dir\"]"
  fi

  if ! colima status >/dev/null 2>&1; then
    info "启动 Colima 虚拟机（CPU ${COLIMA_CPU} 核，内存 ${COLIMA_MEMORY}GB，首次需下载镜像，约几分钟）..."
    colima start --cpu "$COLIMA_CPU" --memory "$COLIMA_MEMORY" --disk 40
  fi
  wait_docker || fail "Docker 运行环境启动失败，可执行 colima start 查看原因"
  info "Docker（Colima）已就绪"
}

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose -f "$COMPOSE_FILE" "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose -f "$COMPOSE_FILE" "$@"
  else
    fail "未找到 docker compose，请重新运行本脚本安装"
  fi
}

check_ports() {
  # 本项目容器已在运行时，端口由自身占用，不再检查
  [ -n "$(compose ps -q 2>/dev/null)" ] && return 0
  for p in "$WEB_PORT" "$BACKEND_PORT" "$MYSQL_PORT"; do
    if lsof -nP -iTCP:"$p" -sTCP:LISTEN >/dev/null 2>&1; then
      fail "端口 $p 已被占用，可换端口运行，例如：WEB_PORT=8080 BACKEND_PORT=19082 MYSQL_PORT=13307 $0"
    fi
  done
}

wait_backend() {
  info "等待后端启动（首次启动需导入数据库，约 1–3 分钟）..."
  for _ in $(seq 1 180); do
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
  [ "$CN_MIRROR" = "1" ] && info "已启用国内镜像加速（Maven、npm）"
  info "构建镜像并启动服务（首次构建需下载依赖，约 5–15 分钟）..."
  mkdir -p "$ROOT/runtime/upload"
  compose up -d --build
  if wait_backend; then
    info "部署完成"
    echo
    echo "  前台地址：  http://localhost:${WEB_PORT}/bbs/"
    echo "  后端接口：  http://localhost:${BACKEND_PORT}"
    echo "  MySQL：     localhost:${MYSQL_PORT}  用户 root  密码 ${MYSQL_ROOT_PASSWORD}  库 bbsdb"
    echo "  查看日志：  $0 logs backend"
    echo
  else
    fail "后端启动超时，请执行 $0 logs backend 查看原因"
  fi
}

case "${1:-up}" in
  up)      up ;;
  down)    ensure_docker; compose down ;;
  restart) ensure_docker; compose up -d --build --force-recreate; wait_backend && info "已重启" ;;
  status)  compose ps ;;
  logs)    shift; compose logs -f --tail=200 "$@" ;;
  reset)
    read -r -p "将删除数据库和 Redis 的全部数据，确认？[y/N] " ans
    [ "$ans" = "y" ] || [ "$ans" = "Y" ] || { info "已取消"; exit 0; }
    compose down -v
    info "已清空，执行 $0 up 重新部署" ;;
  *) echo "用法: $0 [up|down|restart|status|logs|reset]"; exit 1 ;;
esac
