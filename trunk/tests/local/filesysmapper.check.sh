#!/bin/bash

set -x

ls filesysmapper.stdout || exit 1
COUNT=$( grep -c "file: [abc].dat" < filesysmapper.stdout )
(( $COUNT == 3 )) || exit 1

exit 0
