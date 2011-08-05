#!/bin/bash

set -x

ls $OUTPUT || exit 1

COUNT=$( grep -c "num:" $OUTPUT )
(( $COUNT == 11 )) || exit 1

exit 0
