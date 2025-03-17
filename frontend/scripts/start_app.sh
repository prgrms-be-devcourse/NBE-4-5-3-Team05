#!/bin/bash
# start_app.sh

cd /home/ubuntu/app
npm i

pm2 restart next-app || pm2 start npm --name "next-app" --cwd /home/ubuntu/app -- run start