#!/bin/bash
# 一键启动/停止本地前后端
# 用法: scripts/dev-all.sh start|stop|restart|status
DIR="$(cd "$(dirname "$0")" && pwd)"

case "$1" in
  start)
    "$DIR/dev-backend.sh" start && "$DIR/dev-frontend.sh" start ;;
  stop)
    "$DIR/dev-frontend.sh" stop; "$DIR/dev-backend.sh" stop ;;
  restart)
    "$DIR/dev-frontend.sh" stop; "$DIR/dev-backend.sh" stop; sleep 2
    "$DIR/dev-backend.sh" start && "$DIR/dev-frontend.sh" start ;;
  status)
    "$DIR/dev-backend.sh" status; "$DIR/dev-frontend.sh" status ;;
  *) echo "用法: $0 start|stop|restart|status"; exit 1 ;;
esac
