#!/bin/bash

cp -v $GROUP/data.txt . || exit 1
cp -v $GROUP/*expected . || exit 1
start-coaster-service || exit 1
