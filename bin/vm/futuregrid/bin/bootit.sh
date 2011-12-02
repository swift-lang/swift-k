#!/bin/bash

dir=`dirname $0`
cd $dir
cd ..

source env.sh
source ve/bin/activate

output="output.json"
cloudinitd -l debug -v -v -v boot plan/top.conf -o $output
if [ $? -ne 0 ]; then
    echo "The boot failed.  Check the logs"
    exit 1
fi

echo "getting the hostnames..."
python bin/handleoutput.py $output
if [ $? -ne 0 ]; then
    echo "The output parse failed"
    exit 1
fi
