#!/bin/sh

# Simply transforms an input to an output

OUTPUT=$1
shift
INPUTS=${*}

mkdir -p $( dirname ${INPUT} )
for s in ${INPUTS}
do
  echo ${s}
done > ${OUTPUT}

