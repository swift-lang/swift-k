#!/bin/bash

USERNAME=$RAVEN_USERNAME

if [[ -z $USERNAME ]] 
then
    echo "Remote username not provided. Skipping sites configs"
else
    cat swift.conf  | sed "s/\${env.USER}/$USERNAME/" > tmp && mv tmp swift.conf
fi
