#!/bin/bash 

HOME=$PWD
echo "Current test : $HOME"

TESTS_HOME=$(dirname $HOME)
echo "Tests home : $TESTS_HOME"

ps -u $USER
echo "Clearing all java instances"

ps -u $USER | grep java
if [ "$?" == "0" ]
then
    killall -u $USER java -9
fi

echo "Collecting stats"
for test_case in `ls $TESTS_HOME | grep stage`
do
    echo "$test_case"
    cat $TESTS_HOME/$test_case/*check.stdout
    echo 
done