#!/bin/bash

SELF="count_jobs.sh"
SLEEPTIME=60


BEFORE=$(($RANDOM%$SLEEPTIME))
AFTER=$(($SLEEPTIME-$BEFORE))

sleep $BEFORE
echo  "NODE   $(hostname -f)"
ACTIVE=`ps -u $USER | grep $SELF | wc -l`
echo "ps -u $USER | grep  | wc -l"
echo  "ACTIVE $ACTIVE"
echo  "SPLIT $BEFORE:$AFTER"
sleep $AFTER