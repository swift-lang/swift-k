#!/bin/bash
set -x
cat 007-add-in-proc-add.out | grep 189|| exit 1
exit 0
