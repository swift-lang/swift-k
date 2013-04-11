#!/bin/bash

case $STRESS in
    "S1")
        FILES=10
        ;;
    "S2")
        FILES=1000
        ;;
    "S3")
        FILES=10000
        ;;
    "S4")
        FILES=10000
        ;;
    *)
        FILES=1000
        ;;
esac

export GLOBUS_HOSTNAME="128.135.112.73"
export GLOBUS_TCP_PORT_RANGE=50000,51000
#OVERRIDE_GLOBUS_HOSTNAME "128.135.112.73"

nfiles=${FILES:-10}

rm -rf input
mkdir input
cp $(dirname $GROUP)/data/t? input/
cp $GROUP/getlanduse.pl ./
( cd input
  n=0
  for h in $(seq -w 00 99); do
    for v in $(seq -w 00 99); do
      n=$((n+1))
      if [ $n -gt $nfiles ]; then
        break;
      else
        f=t$(echo $RANDOM | sed -e 's/.*\(.\)/\1/')
        ln $f h${h}v${v}.rgb
      fi
    done
  done
  rm t?
)

#makeinput