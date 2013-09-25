#!/bin/bash

cp $GROUP/coaster-service.conf .
cp $GROUP/data.txt .
if [ ! -f "$HOME/.swift/.coaster-service-pids" ]; then
   start-coaster-service
fi
