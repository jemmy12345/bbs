#!/bin/sh
# Usage: ./ry.sh start|stop|restart|status
APP_NAME=bbs-admin.jar
JVM_OPTS="-Dname=$APP_NAME -Duser.timezone=Asia/Shanghai -Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m"

start() {
  if pgrep -f "$APP_NAME" >/dev/null; then
    echo "$APP_NAME is already running."
  else
    nohup java $JVM_OPTS -jar "$APP_NAME" >/dev/null 2>&1 &
    echo "Started $APP_NAME."
  fi
}

stop() {
  pkill -f "$APP_NAME" || true
  echo "Stopped $APP_NAME if it was running."
}

status() {
  pgrep -af "$APP_NAME" || echo "$APP_NAME is not running."
}

case "$1" in
  start) start ;;
  stop) stop ;;
  restart) stop; sleep 2; start ;;
  status) status ;;
  *) echo "Usage: ./ry.sh start|stop|restart|status"; exit 1 ;;
esac
