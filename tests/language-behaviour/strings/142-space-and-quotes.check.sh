#!/bin/bash
set -x
[ -f 142-space-and-quotes.out ] || exit 1
[ -f 142-space-and-quotes.\ space\ .out ] || exit 1
[ -f 142-space-and-quotes.2\"\ space\ \".out ] || exit 1
[ -f 142-space-and-quotes.3\'\ space\ \'.out ] || exit 1
exit 0
