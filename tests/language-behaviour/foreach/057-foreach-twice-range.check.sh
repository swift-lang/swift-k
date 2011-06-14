#!/bin/bash

set -x

for count in `seq --format "%04.f" 0 1 9`
do
	[ -f "057-foreach-twice-range.first.$count.out" ] || exit 1
	CONTENTS1=$( cat 057-foreach-twice-range.first.$count.out.expected )
	CONTENTS2=$( cat 057-foreach-twice-range.first.$count.out )
	[[ $CONTENTS1 == $CONTENTS2 ]] || exit 1
	[ -f "057-foreach-twice-range.second.$count.out" ] || exit 1
	CONTENTS3=$( cat 057-foreach-twice-range.second.$count.out.expected )
	CONTENTS4=$( cat 057-foreach-twice-range.second.$count.out )
	[[ $CONTENTS3 == $CONTENTS4 ]] || exit 1
done
exit 0

