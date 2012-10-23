#!/bin/bash

set -x

grep "file.txt: true"          500-exists.stdout || exit 1
grep "file-missing.txt: false" 500-exists.stdout || exit 1

exit 0
