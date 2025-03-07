#!/bin/bash
# start_app.sh

cd /home/ec2-user/app

# Example: pass secrets as environment variables
# (You can store these in CodeDeploy environment configuration,
#  or use parameter store or secrets manager)
export JWT_SECRET="$JWT_SECRET"# This references the env var set by CodeDeploy
export TOSS_SECRET="$TOSS_SECRET"
export KAKAO_CLIENT_ID="$KAKAO_CLIENT_ID"

# Start your .jar in background (example)
nohup java -jar myapp.jar > /home/ec2-user/app/app.log 2>&1 &
