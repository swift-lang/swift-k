#!/bin/bash
#SBATCH --job-name=mpi_sum
#SBATCH --output=output%N.%j.out
#SBATCH --error=error%N.%j.err
#SBATCH --time=00:10:00
#SBATCH --partition=westmere
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=12
#module load openmpi
mpirun ./mpi_sum
echo "Done "
