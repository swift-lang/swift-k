#!/bin/bash
set -x
cat 018-less.out | grep true|| exit 1
exit 0
