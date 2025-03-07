#!/bin/bash
# stop_app.sh
PID_FILE="application.pid"

if [ ! -f "$PID_FILE" ]; then
  echo "PID 파일 ($PID_FILE)이 존재하지 않습니다. 프로세스가 이미 종료되었을 수 있습니다."
  exit 0
fi

# PID 파일에서 PID 읽기
PID=$(cat "$PID_FILE")

if [ -z "$PID" ]; then
  echo "PID 파일이 비어있습니다. 프로세스 종료가 불필요합니다."
  rm -f "$PID_FILE"
  exit 0
fi

echo "PID 파일에서 읽은 PID: $PID"

# 해당 PID의 프로세스가 존재하는지 확인
if ps -p "$PID" > /dev/null 2>&1; then
  echo "프로세스 $PID 종료 시도중..."
  kill "$PID"
  sleep 2

  # 프로세스가 종료되었는지 다시 확인
  if ps -p "$PID" > /dev/null 2>&1; then
    echo "프로세스가 정상 종료되지 않아 강제 종료합니다."
    kill -9 "$PID"
  fi
  echo "프로세스 $PID가 종료되었습니다."
else
  echo "PID $PID에 해당하는 프로세스가 존재하지 않습니다."
fi

# 종료 후 PID 파일 삭제
rm -f "$PID_FILE"
exit 0
