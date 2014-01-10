#!/bin/bash

set -x

OUTPUT=$( grep delayed: 202-sprintf-k-array.stdout | cut -d ' ' -f 2 | sort -n )
[[ ${OUTPUT[@]} == "array 4 6 8 10 12" ]] || exit 1

exit 0
