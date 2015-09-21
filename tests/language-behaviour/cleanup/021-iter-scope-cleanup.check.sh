#!/bin/bash

PREFIX="021-iter-scope-cleanup"

if [ -n "$(find . -maxdepth 1 -name "$PREFIX.*.tmp" -print -quit)" ]; then
	echo "A temporary file wasn't removed"
	exit 1
fi

if [ `grep "Cleaning" $PREFIX.stdout | wc -l` != "3" ]; then
	echo "Wrong number of files cleaned"
	exit 1
fi

echo "Everything's ok"
exit 0
