#!/bin/bash

set -x

FILES=( catsn.*.out )
[[ ${#FILES[@]} == 10 ]] || exit 1

for FILE in ${FILES}
do
 grep -q "Hello world" ${FILE} || exit 1
done

exit 0
