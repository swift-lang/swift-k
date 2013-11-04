#!/bin/bash

if [[ $1 == "" ]]; then
    sleep_time=5
else
    sleep_time=$1
fi

date
sleep $sleep_time;
echo "This is process $$"
echo "Hostname : $(hostname -f)"
date