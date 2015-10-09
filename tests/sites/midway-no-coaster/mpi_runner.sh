#!/bin/bash

module list         1>&2
module load openmpi 1>&2
echo "ARGS: $*"     1>&2

MPI_CODE=$1
if [ ! -x $1 ];
then
    "ARG1: $1 is not an executable file"
fi
mpiexec -n 32 /scratch/midway/yadunand/swift-k/tests/sites/midway-no-coaster/mpi/mpi_sum
