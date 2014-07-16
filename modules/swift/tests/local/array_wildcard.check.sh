#!/bin/sh

set -x

grep "how are you" < hw.txt || exit 1

exit 0
