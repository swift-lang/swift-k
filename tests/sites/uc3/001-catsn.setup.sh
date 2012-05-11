#!/bin/bash

mkdir -p ~/work
cp -v $GROUP/data.txt . || exit 1
cp -v $GROUP/*expected . || exit 1
export WORK=$HOME/work
start-coaster-service || exit 1
