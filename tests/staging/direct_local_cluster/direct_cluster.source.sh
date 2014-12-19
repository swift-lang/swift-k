#!/bin/bash

HOST=$(hostname -f)

if   [[ "$HOST" == *midway* ]]; then
    echo "On Midway"
    export SITE="midway"

elif [[ "$HOST" == *beagle* ]]; then
    echo "On Beagle"
    export SITE="beagle"

elif [[ "$HOST" == *mcs* ]]; then
    echo "On MCS"
    export SITE="mcs"

elif [[ "$HOST" == *osgconnect* ]]; then
    echo "On Osgconnect"
    export SITE="osgconnect"

elif [[ "$HOST" == *blogin[0-9]*lcrc* ]]; then
    echo "On Blues"
    export SITE="blues"

elif [[ "$HOST" == *flogin[0-9]*lcrc* ]]; then
    echo "On Fusion"
    export SITE="fusion"

elif [[ "$HOST" == *communicado* ]]; then
    echo "On Fusion"
    export SITE="fusion"

elif [[ "$HOST" == *bridled* ]]; then
    echo "On Fusion"
    export SITE="fusion"

else
    echo "On unidentified machine, using defaults"
fi
