#!/bin/bash

for file in `ls data/tmp.*|grep -v out`
do
   if [ ! -f "$file.out" ]; then
      echo $file.out was not created
      exit 1
   fi

   FILE_SUM=`sum $data/$file`
   OUT_SUM=`sum $data/$file.out`

   if [ "$FILE_SUM" != "$OUT_SUM" ]; then
      echo Checksums of $file and $file.out are not the same
      exit 1
   fi
done   
