#!/bin/bash

set -x

ls stdout.txt || exit 1

COUNT=$( grep -c "num:" stdout.txt )
(( $COUNT == 11 )) || exit 1

exit 0
