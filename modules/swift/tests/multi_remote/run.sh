#!/bin/bash

# Start keychain and point it to private key                                               
/home/yadunand/bin/keychain ~/.ssh/id_rsa

# Let the shell know the agent                                                             
source ~/.keychain/midway001-sh > /dev/null

./remote_driver.setup.sh

echo "Running remote_driver2.swift"
#swift -tc.file tc.data.2 -config cf -sites.file multiple.xml.bak remote_driver2.swift
#swift -tc.file tc.data.2 -config cf -sites.file multiple.xml remote_driver2.swift
swift -tc.file tc.data -config cf -sites.file $SITES remote_driver.swift | tee root_level.LOG

rm $SITES