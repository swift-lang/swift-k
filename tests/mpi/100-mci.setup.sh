#!/bin/bash

set -x

which mpicc || exit 1

mpicc -std=gnu99 $GROUP/100-mpi-cp.c -o $GROUP/mpi-cp || exit 1

cp -v $GROUP/100-input.txt . || exit 1

exit 0
