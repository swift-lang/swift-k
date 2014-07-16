#!/bin/bash

LOG="$RANDOM.log"

echo "Running with $(swift -version)" >> $LOG
for testcase in $(ls *.swift)
do
    echo "STARTING $testcase-----------------------------------" | tee $LOG
    swift $testcase | tee -a $LOG
    echo "ENDING $testcase-------------------------------------" | tee $LOG
done
