#!/bin/bash
# start_app.sh

cd /home/ubuntu/app

# Example: pass secrets as environment variables
# (You can store these in CodeDeploy environment configuration,
#  or use parameter store or secrets manager)
# This references the env var set by CodeDeploy
export JWT_SECRET=$(aws ssm get-parameter --name /team5/JWT_SECRET --with-decryption --query "Parameter.Value" --output text)
export TOSS_SECRET=$(aws ssm get-parameter --name /team5/TOSS_SECRET --with-decryption --query "Parameter.Value" --output text)
export KAKAO_CLIENT_ID=$(aws ssm get-parameter --name /team5/KAKAO_CLIENT_ID --with-decryption --query "Parameter.Value" --output text)

# Start your .jar in background (example)
nohup java -jar /home/ubuntu/app/build/libs/*.jar > /home/ubuntu/app/app.log 2>&1 &
