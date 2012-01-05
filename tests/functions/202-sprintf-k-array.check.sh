#!/bin/bash

set -x

# Grab partial output
OUTPUT=$( grep delayed: 202-sprintf-k-array.stdout | head -4 | cut -d ' ' -f 2 )
[[ $? == 0 ]] || exit 1

# NOTE: we cannot guarantee that the "12" is before "array"

# First four outputs are in order
[[ ${OUTPUT[@]} == "4 6 8 10" ]] || exit 1

# Grab whole output
OUTPUT=$( grep delayed: 202-sprintf-k-array.stdout | cut -d ' ' -f 2 )

# Output "10" is before "array"
echo ${OUTPUT[@]} | grep "10.*array"
[[ $? == 0 ]] || exit 1

exit 0
