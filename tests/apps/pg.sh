#!/bin/sh

# Generates multiple PG outputs, bundles them into OUTPUT tarfile
# INDEX contains names of PG outputs and
#             is compatible with Swift readData()

INPUT=$1
DIRECTORY=$2
OUTPUT=$3
INDEX=$4

set -x

# Operate in directory (run-*)
mkdir -p ${DIRECTORY}
pushd ${DIRECTORY}

# Create outputs
PG_OUTS=$( echo pg-{a,b,c}.out )
touch ${PG_OUTS[@]}
TAR=$( basename ${OUTPUT} )
tar cf ${TAR} ${PG_OUTS[@]}

# Make index
popd
for s in ${PG_OUTS[@]}
do
  echo ${DIRECTORY}/${s}
done > ${INDEX}
