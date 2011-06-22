#!/bin/bash
set -x
cat 016-greatereq.out | grep false|| exit 1
exit 0
