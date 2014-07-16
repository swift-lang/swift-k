#!/bin/sh

# Concatenate the INPUT and TEXT to the OUTPUT file

INPUT=$1
TEXT=$2
OUTPUT=$3

{
  cat ${INPUT}
  echo ${TEXT}
} > ${OUTPUT}
