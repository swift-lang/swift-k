#!/bin/sh

set -x

COUNT=$( ls 201-output* | grep -c )

[[ ${COUNT} == 40 ]] || exit 1

exit 0
