#!/bin/bash

set -x

grep "file.txt: true"          stdout.txt || exit 1
grep "file-missing.txt: false" stdout.txt || exit 1

exit 0
