#!/bin/bash

for aline in `ls -1 /tmp/outdir`
do
theline= echo $aline | cut -c 1-3
if [ $theline == "out" ]
then
 ; #pass
else
 exit 1 #fail
fi

