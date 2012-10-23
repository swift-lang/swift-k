#!/bin/bash
set -x
cat 0053-toint.out | grep 109|| exit 1
exit 0
