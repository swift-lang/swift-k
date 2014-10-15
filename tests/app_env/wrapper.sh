#!/bin/bash

echo "DOCKERID: $DOCKERID"
if [ -z $DOCKERID ]
then
    echo "The env variable is not set"
    exit -1
fi

