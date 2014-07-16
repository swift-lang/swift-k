#!/bin/sh

LOG=$1
OUTPUT=$2

iso-to-secs < ${LOG} | grep RUNID | awk '{ print $1 }' > ${OUTPUT}
