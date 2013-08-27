#!/bin/sh

set -x

COUNT=$( ls 201-output* | wc -l )

[[ $COUNT == 40 ]] || exit 1

exit 0
