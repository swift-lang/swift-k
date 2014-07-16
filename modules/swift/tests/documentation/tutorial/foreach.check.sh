#!/bin/bash

set -x

grep "1 foreach.1.txt" foreach.1.count || exit 1
grep "2 foreach.2.txt" foreach.2.count || exit 1
grep "3 foreach.3.txt" foreach.3.count || exit 1

exit 0
