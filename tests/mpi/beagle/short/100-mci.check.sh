#!/bin/bash

set -x

[ -f 100-output.txt ] || exit 1

CONTENTS1=$( cat 100-input.txt )
CONTENTS2=$( cat 100-output.txt )

[[ $CONTENTS1 == $CONTENTS2 ]] || exit 1

exit 0
