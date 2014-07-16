#!/bin/bash
set -x
cat 004-strcat-in-arg.out | grep test004append|| exit 1
exit 0
