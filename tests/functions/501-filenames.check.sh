#!/bin/bash

sum1=`sum 501-filenames.out`
sum2=`sum 501-filenames.out.expected`

if [ "$sum1" != "$sum2" ]; then
   echo Error: Created file does not match expected output
   exit 1
fi

exit 0
