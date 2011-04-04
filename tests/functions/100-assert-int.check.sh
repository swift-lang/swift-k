#!/bin/bash

set -x

grep "assert failed" stdout.txt || exit 1

exit 0
