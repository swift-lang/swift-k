#!/bin/sh

SWIFT_HOME=$( dirname $( dirname $0 ) )
LOG=${SWIFT_HOME}/etc/cdm_broadcast.log

bgp_broadcast()
{
  DIR=$1
  FILE=$2
  DEST=$3
  if [[ ! -f ips.list ]] 
    then
    BLOCKS=$( qstat -u ${USER} | grep ${USER} | awk '{ print $6 }' )
    IPS=$( listip ${BLOCKS} )
    for IP in ${IPS}
     do
     echo ${IP}
    done >> ip.list
  else
    while read T
     do 
     BLOCKS="$BLOCKS $T" 
    done < ip.list
  fi
  for IP in ${BLOCKS}
   do
   ssh ${IP} /bin.rd/f2cn ${DIR}/${FILE} ${DEST}/${FILE}
  done
}

local_broadcast()
{
  DIR=$1
  FILE=$2
  DEST=$3
  cp -v ${FILE} ${DEST}/${FILE}
}
  
{
  declare -p PWD
    set -x

    FILE=$1
    DIR=$2
    DEST=$3

    if [[ $( uname -p ) == "ppc64" ]]
      then
      bgp_broadcast ${DIR} ${FILE} ${DEST}
    else 
      bgp_local ${DIR} ${FILE} ${DEST}
    fi

} >> ${LOG} 2>&1
