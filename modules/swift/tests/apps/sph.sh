#!/bin/sh

set -x

TARFILE=$1
ENTRY=$2
OUTPUT=$3

# Entry name contains directory: remove it
ENTRY=$( basename ${ENTRY} )

tar xf ${TARFILE} ${ENTRY} || exit 1
touch ${OUTPUT}

exit 0
