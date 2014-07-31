#!/bin/sh

NAME=$1
CSV_OUT=$2

{
    echo "m"
    touch $NAME
    echo $NAME
} > $CSV_OUT 
