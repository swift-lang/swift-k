#!/bin/bash

set -x

ls $OUTPUT || exit 1
COUNT=$( grep -c "file: [abc].dat" < $TEST_LOG )
(( $COUNT == 3 )) || exit 1

exit 0
