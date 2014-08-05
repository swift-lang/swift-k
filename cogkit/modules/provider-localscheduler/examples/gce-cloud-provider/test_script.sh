#!/bin/bash

LOG=/var/log/start.script.output
touch $LOG

echo "$HOSTNAME" >> $LOG
echo "$(date)" >> $LOG
env >> $LOG

