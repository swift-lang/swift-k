#!/bin/bash

set -x

OUTPUT=$( grep delayed: 201-sprintf-k.stdout | cut -d ' ' -f 2 | sort -n )
[[ ${OUTPUT[@]} == "4 6 8 10 12" ]] || exit 1

exit 0
