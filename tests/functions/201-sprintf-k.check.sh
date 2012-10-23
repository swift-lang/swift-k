#!/bin/bash

set -x

OUTPUT=$( cat 201-sprintf-k.stdout )
[[ $? == 0 ]] || exit 1
echo $OUTPUT grep "ready.*delayed"
[[ $? == 0 ]] || exit 1

OUTPUT=$( grep delayed: 201-sprintf-k.stdout | cut -d ' ' -f 2 )
[[ $? == 0 ]] || exit 1
[[ ${OUTPUT[@]} == "4 6 8 10 12" ]] || exit 1

exit 0
