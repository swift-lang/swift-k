#!/bin/bash

cd `dirname $0`

source env.sh
python bin/virtualenv.py ve
if [ $? -ne 0 ]; then
    echo "Failed to created the needed python virtual environment"
    exit 1
fi

source ve/bin/activate
easy_install cloudinitd
if [ $? -ne 0 ]; then
    echo "Failed to install cloudinitd"
    exit 1
fi

echo "Registering the key names in all the clouds"
python bin/register_key.py hosts.txt
if [ $? -ne 0 ]; then
    echo "Failed to register the key names"
    exit 1
fi

echo "Success!"
echo ""
echo 0
