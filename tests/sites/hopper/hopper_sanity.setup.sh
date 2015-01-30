#!/bin/bash

echo  "BEAGLE_USERNAME : $BEAGLE_USERNAME"
echo  "MIDWAY_USERNAME : $MIDWAY_USERNAME"
echo  "MCS_USERNAME    : $MCS_USERNAME"
echo  "UC3_USERNAME    : $UC3_USERNAME"
USERNAME=$BEAGLE_USERNAME

if [[ -z $USERNAME ]]
then
    echo "Remote username not provided. Skipping sites configs"
else
    ls swift.conf
    cat swift.conf  | sed "s/\${env.USER}/$USERNAME/" > tmp && mv tmp swift.conf
fi
