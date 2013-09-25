#!/bin/bash

set -x

# [ -f 100-output.txt ] || exit 1

grep -q howdy transform-*.txt || exit 1

exit 0
