#!/bin/bash

SWIFTSCRIPT=x_func.swift;

SWIFT_ITERATIONS=`grep "#NIGHTLY" $SWIFTSCRIPT`;
if [ "$?" == "0" ]; then
    echo "SWIFT_ITERATIONS : $SWIFT_ITERATIONS";
    SWIFT_ITERATIONS=($SWIFT_ITERATIONS);
    echo "${SWIFT_ITERATIONS[1]}";
    echo "${SWIFT_ITERATIONS[2]}";
    
fi;
