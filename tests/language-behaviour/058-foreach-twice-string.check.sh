#!/bin/bash

set -x

for count in `seq --format "%04.f" 0 1 2`
do
	[ -f "058-foreach-twice-string.first.$count.out" ] || exit 1
	CONTENTS1=$( cat 058-foreach-twice-string.first.$count.out.expected )
	CONTENTS2=$( cat 058-foreach-twice-string.first.$count.out )
	[[ $CONTENTS1 == $CONTENTS2 ]] || exit 1
	[ -f "058-foreach-twice-string.second.$count.out" ] || exit 1
	CONTENTS3=$( cat 058-foreach-twice-string.second.$count.out.expected )
	CONTENTS4=$( cat 058-foreach-twice-string.second.$count.out )
	[[ $CONTENTS3 == $CONTENTS4 ]] || exit 1
done
exit 0

