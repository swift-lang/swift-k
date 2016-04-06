#!/bin/bash

#set -x

mname=$(hostname)

incarfile=$2
poscarfile=$3
potcarfile=$4
kpointsfile=$5

outcarfile=$6
contcarfile=$7

cp $incarfile .
cp $poscarfile .
cp $potcarfile .
cp $kpointsfile .

# vesta and mira has different path than cetus
if [[ $mname == *vesta* || $mname == *mira* ]]
then
    export PATH=/soft/cobalt/bgq_hardware_mapper:$PATH
else
    export PATH=/soft/cobalt/cetus/bgq_hardware_mapper:$PATH    
fi

#Run the preprocessing script
#/bin/bash $preproc "$@"

#export SUBBLOCK_SIZE=16

# Prepare shape based on subblock size
# provided by user in sites environment
case "$SUBBLOCK_SIZE" in
1) SHAPE="1x1x1x1x1"
;;
8) SHAPE="1x2x2x2x1"
;;
16) SHAPE="2x2x2x2x1"
;;
32) SHAPE="2x2x2x2x2"
;;
64) SHAPE="2x2x4x2x2"
;;
128) SHAPE="2x4x4x2x2"
;;
256) SHAPE="2x4x4x4x2"
;;
512) SHAPE="4x4x4x4x2"
;;
*) echo "SUBBLOCK_SIZE not set or incorrectly set: will not use subblock jobs"
;;
esac

# If subblock size is provided, do subblock business
if [ "_$SUBBLOCK_SIZE" != "_" ]
then
    # sub-block size larger than 512 nodes, currently untested
    if [ "$BOOTABLE"_ != "_" ]
    then
        export SWIFT_SUBBLOCKS=$(get-bootable-blocks --size $SUBBLOCK_SIZE --geometry $SHAPE $COBALT_PARTNAME)
        export SWIFT_SUBBLOCK_ARRAY=($SWIFT_SUBBLOCKS)

        if [ "_$SWIFT_SUBBLOCKS" = "_" ]; then
          echo ERROR: "$0": SWIFT_SUBBLOCKS is null.
          exit 1
        fi

        BLOCK=${SWIFT_SUBBLOCK_ARRAY[$SWIFT_JOB_SLOT]}
        
        #Some logging
        echo "$0": running BLOCK="$BLOCK" SLOT="$SWIFT_JOB_SLOT"
        echo "$0": running cmd: "$0" args: "$@"
        echo "$0": running runjob --block "$BLOCK" : "$@"
        
        boot-block --block $BLOCK
        runjob --block $BLOCK -p 8 --np "$((8*$SUBBLOCK_SIZE))" : $1
        boot-block --block $BLOCK --free 

        echo "Runjob finished"
    else
        export SWIFT_SUBBLOCKS=$(get-corners.py "$COBALT_PARTNAME" $SHAPE)
        export SWIFT_SUBBLOCK_ARRAY=($SWIFT_SUBBLOCKS)
        
        if [ "_$SWIFT_SUBBLOCKS" = _ ]; then
          echo ERROR: "$0": SWIFT_SUBBLOCKS is null.
          exit 1
        fi
        
        nsb=${#SWIFT_SUBBLOCK_ARRAY[@]}
        
        CORNER=${SWIFT_SUBBLOCK_ARRAY[$SWIFT_JOB_SLOT]}
        
        #Some logging
        #processedargs=$(echo "$@" | cut -d" " -f 1)
        echo "$0": running BLOCK="$COBALT_PARTNAME" SLOT="$SWIFT_JOB_SLOT"
        echo "$0": running cmd: "$0" args: "$@"
        echo "$0": runjob --block "$COBALT_PARTNAME" --corner "$CORNER" --shape "$SHAPE" -p 8 --np "$((8*$SUBBLOCK_SIZE))" : "$@" 
        
        #without timeout
        #runjob --strace none --block "$COBALT_PARTNAME" --corner "$CORNER" --shape "$SHAPE" -p 16 --np "$((16*$SUBBLOCK_SIZE))" : "$@"
        runjob --block "$COBALT_PARTNAME" --corner "$CORNER" --shape "$SHAPE" -p 4 --np "$((4*$SUBBLOCK_SIZE))" : $1
        
        echo "Runjob finished."
    fi
else
    # run w/o subblocks if no subblock size provided
    echo "Running in nonsubblock mode."
    echo "$0": running runjob -p 16 --block $COBALT_PARTNAME : "$@"

    #strace -o "$HOME/strace.runjob.out" runjob --strace none -p 16 --block $COBALT_PARTNAME : "$@"
    runjob -p 16 --block $COBALT_PARTNAME : "$@"

    echo "Finished Running in nonsubblock mode."
fi

#Run the postprocessing script
#/bin/bash $postproc "$@"
mv OUTCAR $outcarfile
mv CONTCAR $contcarfile

exit 0

