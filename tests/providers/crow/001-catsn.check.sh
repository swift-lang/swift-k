#!/bin/bash

set -x

COUNT=$( ls catsn.*.out | wc -l )
[[ $COUNT == 10 ]] || exit 1

exit 0
