#!/bin/bash

source setup.sh

for i in $(seq 1 1 3)
do
    pushd .
    echo "============================TESTING part0$i==========================="
    cd part0$i
    swift p$i.swift -site=local
    if [[ $? == 0 ]]
    then
        echo "Cleaning up!"
        cleanup
    fi
    echo -e "\n\n"
    popd
done

SITES=('beagle-remote' 'midway-remote' 'osgconnect-remote' "$1")
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
