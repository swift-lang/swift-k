#!/bin/bash

PREFIX="001-proc-scope-cleanup"

if [ -n "$(find . -maxdepth 1 -name "$PREFIX.*.tmp" -print -quit)" ]; then
	echo "A temporary file wasn't removed"
	exit 1
fi

if [ ! -f $PREFIX.t2.out ]; then
	echo "A persistent file was removed"
	exit 1
fi

if ! grep "^Cleaning file .*$PREFIX.t1.tmp" $PREFIX.stdout; then
	echo "Test mapper did not claim to clean t1"
	exit 1
fi

if grep "^Cleaning file .*$PREFIX.t2.out" $PREFIX.stdout; then
	echo "Test mapper did not identify t2 as persistent"
	exit 1
fi

if ! diff $PREFIX.out $GROUP/$PREFIX.out.expected; then
	echo "Output differs"
	exit 1
fi

echo "Everything's ok"
exit 0
