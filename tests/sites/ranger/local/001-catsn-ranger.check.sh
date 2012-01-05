#!/bin/bash

set -x

for count in `seq --format "%04.f" 1 1 10`
do
	[ -f catsn.$count.out ] || exit 1
	CONTENTS1=$( cat catsn.$count.out.expected )
	CONTENTS2=$( cat catsn.$count.out )
	[[ $CONTENTS1 == $CONTENTS2 ]] || exit 1
done
exit 0
