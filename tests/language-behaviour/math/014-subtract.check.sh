#!/bin/bash
set -x
cat 014-subtract.out | grep '\-37'|| exit 1
exit 0
