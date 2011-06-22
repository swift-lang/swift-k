#!/bin/bash
set -x
cat 009-multiply.out | grep 42|| exit 1
exit 0
