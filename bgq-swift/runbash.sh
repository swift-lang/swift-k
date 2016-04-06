#!/bin/bash

#To run:
qsub -n 512 -t 02:00:00 --mode script subjob.sh

#qsub -e WORKER_LOGGING_LEVEL=NONE --proccount 32 -n 32 -t 40

