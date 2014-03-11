#!/bin/bash
ARG1=$1
ls | grep "$1" &> /dev/null
if [ $? == 0 ]
then
    echo "Hey this is wrapper and the $1 exists as a file";
    ls -lah;
else
    echo "Doinks! the file we need isn't here";
    ls -lah
fi
cat $ARG1 > $ARG1.test
if [ $? == 0 ]
then
    echo "The cat worked! ";
else
    echo "The cat failed ";
fi

rm $ARG1 $ARG1.test
echo "Residual files cleaned up"
