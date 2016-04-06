#!/bin/bash

qsub -A ExM -t 10 -n 1 --mode script cobaltvasp.sh
