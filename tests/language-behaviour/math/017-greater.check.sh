#!/bin/bash
set -x
cat 017-greater.out | grep false|| exit 1
exit 0
