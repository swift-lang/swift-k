#!/bin/sh

SWIFT_HOME=$( dirname $( dirname $0 ) )
LOG=${SWIFT_HOME}/etc/cdm_broadcast.log
{
    set -x

    FILE=$1
    DIR=$2
    DEST=$3
    
    cp -v ${DIR}/${FILE} ${DEST}

} >> ${LOG} 2>&1
