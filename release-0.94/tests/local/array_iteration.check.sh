#!/bin/bash

set -x

FILES=( $( ls f[1-3].txt ) )
(( ${#FILES[@]} == 3 )) || exit 1

grep "hi there" < ${FILES[1]} || exit 1

exit 0
