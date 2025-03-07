#!/bin/bash
# stop_app.sh
PID_FILE="application.pid"

pwd

# PID 파일이 존재하면 처리하고, 없으면 그냥 exit 0
if [ -f "$PID_FILE" ]; then
  PID=$(cat "$PID_FILE")
  if [ -n "$PID" ] && ps -p "$PID" > /dev/null 2>&1; then
    echo "프로세스 $PID 종료 시도중..."
    kill "$PID"
    sleep 2
    if ps -p "$PID" > /dev/null 2>&1; then
      echo "프로세스가 정상 종료되지 않아 강제 종료합니다."
      kill -9 "$PID"
    fi
    echo "프로세스 $PID가 종료되었습니다."
  else
    echo "PID 파일은 존재하지만, PID ($PID)는 현재 실행 중이지 않습니다."
  fi
  rm -f "$PID_FILE"
else
  echo "PID 파일이 없으므로 종료할 프로세스가 없습니다."
fi

exit 0
