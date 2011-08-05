#!/bin/bash

set -x

ls $TEST_LOG || exit 1

COUNT=$( grep -c "num:" $TEST_LOG )
(( $COUNT == 11 )) || exit 1

exit 0
