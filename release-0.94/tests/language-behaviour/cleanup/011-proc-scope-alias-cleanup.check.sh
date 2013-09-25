#!/bin/bash

PREFIX="011-proc-scope-alias-cleanup"

if [ -n "$(find . -maxdepth 1 -name "$PREFIX.*.tmp" -print -quit)" ]; then
	echo "A temporary file wasn't removed"
	exit 1
fi

if [ ! -f $PREFIX.2.out ]; then
	echo "A persistent file was removed"
	exit 1
fi

if ! grep "Remapping" $PREFIX.stdout; then
	echo "Test mapper did not claim to have remapped anything"
	exit 1
fi

if ! grep "Cleaning file file://.*$PREFIX.1.tmp" $PREFIX.stdout; then
	echo "Test mapper didn't clean what it should have"
	exit 1
fi

if ! grep "Not cleaning file://.*$PREFIX.2.out" $PREFIX.stdout; then
	echo "Test mapper did not identify 2 as persistent"
	exit 1
fi

if ! diff $PREFIX.out $GROUP/$PREFIX.out.expected; then
	echo "Output differs"
	exit 1
fi

echo "Everything's ok"
exit 0
