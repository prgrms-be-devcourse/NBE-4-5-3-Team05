#!/bin/bash
# start_app.sh

pm2 restart next-app || pm2 start npm --name "next-app" -- run start