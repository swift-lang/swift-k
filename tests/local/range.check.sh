#!/bin/bash

set -x

ls range.stdout || exit 1

COUNT=$( grep -c "num:" range.stdout )
(( $COUNT == 11 )) || exit 1

exit 0
