#!/bin/bash
set -x
cat 0191-not-equals.out | grep true || exit 1
exit 0
