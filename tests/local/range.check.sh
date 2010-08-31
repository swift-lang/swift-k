#!/bin/sh

set -x

ls stdout.txt || exit 1

COUNT=$( grep -c "trace:" stdout.txt )
(( $COUNT == 11 )) || exit 1

exit 0
