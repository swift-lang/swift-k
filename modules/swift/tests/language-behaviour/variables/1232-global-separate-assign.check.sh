#!/bin/bash
set -x
cat 1232-global-separate-assign.out | grep hi|| exit 1
exit 0
