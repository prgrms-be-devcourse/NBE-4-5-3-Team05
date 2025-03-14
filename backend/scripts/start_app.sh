#!/bin/bash
# start_app.sh

PID_FILE="application.pid"

cd /home/ubuntu/app

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



# Example: pass secrets as environment variables
# (You can store these in CodeDeploy environment configuration,
#  or use parameter store or secrets manager)
# This references the env var set by CodeDeploy
export JWT_SECRET=$(aws ssm get-parameter --name /team5/JWT_SECRET --with-decryption --query "Parameter.Value" --output text)
export TOSS_SECRET=$(aws ssm get-parameter --name /team5/TOSS_SECRET --with-decryption --query "Parameter.Value" --output text)
export KAKAO_CLIENT_ID=$(aws ssm get-parameter --name /team5/KAKAO_CLIENT_ID --with-decryption --query "Parameter.Value" --output text)
export AWS_ACCESS_KEY=$(aws ssm get-parameter --name /team5/AWS_ACCESS_KEY --with-decryption --query "Parameter.Value" --output text)
export AWS_S3_BUCKET_NAME=$(aws ssm get-parameter --name /team5/AWS_S3_BUCKET_NAME --with-decryption --query "Parameter.Value" --output text)
export AWS_SECRET_KEY=$(aws ssm get-parameter --name /team5/AWS_SECRET_KEY --with-decryption --query "Parameter.Value" --output text)
export MAIL_PASSWORD=$(aws ssm get-parameter --name /team5/MAIL_PASSWORD --with-decryption --query "Parameter.Value" --output text)
export REDIS_PORT=$(aws ssm get-parameter --name /team5/REDIS_PORT --with-decryption --query "Parameter.Value" --output text)
export REDIS_HOST=$(aws ssm get-parameter --name /team5/REDIS_HOST --with-decryption --query "Parameter.Value" --output text)
export DB_URL=$(aws ssm get-parameter --name /team5/DB_URL --with-decryption --query "Parameter.Value" --output text)
export DB_USERNAME=$(aws ssm get-parameter --name /team5/DB_USERNAME --with-decryption --query "Parameter.Value" --output text)
export DB_PASSWORD=$(aws ssm get-parameter --name /team5/DB_PASSWORD --with-decryption --query "Parameter.Value" --output text)
export SERVER_HOST=$(aws ssm get-parameter --name /team5/SERVER_HOST --with-decryption --query "Parameter.Value" --output text)



# Start your .jar in background (example)
nohup java -jar /home/ubuntu/app/build/libs/*.jar --spring.profiles.active=prod,secret,monitor > /home/ubuntu/app/app.log 2>&1 &
