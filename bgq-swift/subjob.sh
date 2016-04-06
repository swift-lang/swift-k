#!/bin/bash

#To run:
#qsub -n 256 -t 45 --mode script runbash.sh

# Note: This path is for Cetus only
#export PATH=/soft/cobalt/cetus/bgq_hardware_mapper:$PATH
# Note: This path is for Mira or Vesta
export PATH=/soft/cobalt/bgq_hardware_mapper:$PATH

#build app
mpixlc mpicatnap.c -o mpicatnap
bgxlc mysleep.c -o mysleep

SHAPE="2x2x2x2x1"
CORNERS=$(get-corners.py $COBALT_PARTNAME $SHAPE)


# Set this low for testing.
# Consult with systems if you need it > 128
MAXRUNJOB=64

i=0
for j in $(seq 1 1024)
do
  for CORNER in $CORNERS
   do
     echo "Run $i corner $CORNER"
     stime=$(shuf -i 1-10 -n 1)
     #with timeout
     #runjob --block $COBALT_PARTNAME --corner $CORNER --shape $SHAPE -p 1 --np 8 --timeout 30 : mpicatnap in.data out.data $stime >RUNJOB.$j-$i 2>&1 &
     
     #without timeout
     #(echo "About to use this corner: $CORNER" >RUNJOB.$j-$i; runjob --block $COBALT_PARTNAME --corner $CORNER --shape $SHAPE -p 16 -n 128 : ./mysleep 1 >>RUNJOB.$j-$i 2>&1 ; echo Exit status: $? >>RUNJOB.$j-$i) &
     (echo "About to use this corner: $CORNER" >RUNJOB.$j-$i; runjob --block $COBALT_PARTNAME --corner $CORNER --shape $SHAPE -p 16 -n 64 : mpicatnap in.data out.data $stime >>RUNJOB.$j-$i 2>&1 ; echo Exit status: $? >>RUNJOB.$j-$i) &
     
     # Important - give some time for runjob to get initialized
     sleep 3
     i=$((i+1))
     if [ $i -ge $MAXRUNJOB ]; then
       echo "Reached MAXRUNJOB $MAXRUNJOB"
       break;
     fi
  done
  # The runjobs were backgrounded, wait for them to finish
  wait
  echo "Done $j"
  i=0
done

# Cobalt job ends when this script exits
exit 0

