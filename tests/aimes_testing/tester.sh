#!/bin/bash

run_script(){
    start=`date +%s`
    swift $* &> $RANDOM.log
    end=`date +%s`
    runtime=$((end-start))
    echo "$1 $2 $runtime"
}

run_script "bag_of_tasks.swift" "-N=100"
run_script "bag_of_tasks.swift" "-N=200"
run_script "bag_of_tasks.swift" "-N=400"
run_script "bag_of_tasks.swift" "-N=800"
run_script "bag_of_tasks.swift" "-N=1600"

