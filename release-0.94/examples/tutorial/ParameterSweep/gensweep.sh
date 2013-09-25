#! /bin/sh

#
# gensweep.sh - Generate per-member and common parameter files
#               for a parameter sweep (an ensemble of simulations).
#               Generates 2-column files of the form: "paramter value" 

# echo gensweep.sh: $0 $* >/dev/tty # For debugging on localhost

# Determine the filename patterns from the supplied zero'th file names

nMembers=$1
mBase=$(basename $2 .0)
mDir=$(dirname  $2)
mName=$mDir/$mBase

nCommon=$3
cBase=$(basename $4 .0)
cDir=$(dirname  $4)
cName=$cDir/$cBase

# Generate an input file for each simulation in the ensemble

for (( m=0; m<nMembers; m++ )); do
  echo n    $m       >$mName.$m
  echo rate $RANDOM >>$mName.$m
  echo dx   $RANDOM >>$mName.$m
done

# Generate the input files common to all simulations in the ensemble

for (( c=0; c<nCommon; c++ )); do
  echo c     $c       >$cName.$c
  echo alpha $RANDOM >>$cName.$c
  echo beta  $RANDOM >>$cName.$c
done
