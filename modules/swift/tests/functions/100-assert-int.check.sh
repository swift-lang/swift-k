#!/bin/bash

set -x

grep "assert failed" 100-assert-int.stdout || exit 1

exit 0
