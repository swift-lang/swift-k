#!/bin/sh

set -x

ls stdout.txt || exit 1
COUNT=$( grep -c "file: [abc].dat" < stdout.txt )
(( $COUNT == 3 )) || exit 1

exit 0
