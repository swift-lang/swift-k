#!/bin/bash

source setup.sh

export BEAGLE_USERNAME="yadunandb"
export MIDWAY_USERNAME="yadunand"
export AWS_CREDENTIALS_FILE="/home/yadu/.ssh/swift-grant-credentials.csv"
export URL_OF_AD_HOC_MACHINE_1="crank"
export AD_HOC_1_USERNAME="yadunand"
export OSG_USERNAME="yadunand"

if 0
then
for i in $(seq 1 1 3)
do
    pushd .
    echo "============================TESTING part0$i==========================="
    cd part0$i
    swift p$i.swift -site=localhost
    if [[ $? == 0 ]]
    then
        echo "Cleaning up!"
        cleanup
    fi
    echo -e "\n\n"
    popd
done
fi

SITES=('beagle' 'midway' 'osgc' 'ad-hoc-1')
for i in $(seq 4 1 6)
do
    pushd .
    echo "============================TESTING part0$i==========================="
    cd part0$i

    for SITE in ${SITES[*]}
    do
        echo "Running on SITE : $SITE"
        swift p$i.swift -site=$SITE
        if [[ $? == 0 ]]
        then
            echo "Cleaning up!"
            cleanup
        fi
    done
    echo -e "\n\n"
    popd
done
