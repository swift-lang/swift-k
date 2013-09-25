#!/bin/bash

NPOINTS=${1:-100}

awk "
BEGIN {
  srand($RANDOM)
  for (i=0;i<$NPOINTS;i++) {
    x=rand()
    y=rand()
    radius=sqrt((x*x)+(y*y))
    print x, y, radius
  } 
}"
