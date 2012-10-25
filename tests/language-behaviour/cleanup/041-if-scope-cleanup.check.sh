#!/bin/bash

PREFIX="041-if-scope-cleanup"

if [ -n "$(find . -name "$PREFIX.*.tmp" -print -quit)" ]; then
	echo "A temporary file wasn't removed"
	exit 1
fi

if [ ! -f $PREFIX.else.1.1.out ] || [ ! -f $PREFIX.then.2.0.out ]; then
	echo "A persistent file was removed"
	exit 1
fi

if [ `grep "Cleaning" $PREFIX.stdout | wc -l` != "2" ]; then
	echo "Wrong number of files cleaned"
	exit 1
fi

echo "Everything's ok"
exit 0
