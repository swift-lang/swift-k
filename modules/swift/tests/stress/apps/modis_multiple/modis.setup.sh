#!/bin/bash

if [[ -z $MIDWAY_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{mid.USER}/$MIDWAY_USERNAME/" > tmp && mv tmp sites.xml
fi
if [[ -z $UC3_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{uc3.USER}/$UC3_USERNAME/" > tmp && mv tmp sites.xml
fi
if [[ -z $BEAGLE_USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls *xml
    cat sites.xml  | sed "s/{beagle.USER}/$BEAGLE_USERNAME/" > tmp && mv tmp sites.xml
fi

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

nfiles=${FILES:-10}
#OVERRIDE_GLOBUS_HOSTNAME "swift.rcc.uchicago.edu"
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