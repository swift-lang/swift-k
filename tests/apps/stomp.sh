#!/bin/sh

# Simply transforms an input to an output

INPUT=$1
OUTPUT=$2

mkdir -pv $( dirname ${OUTPUT} )

cp ${INPUT} ${OUTPUT}
