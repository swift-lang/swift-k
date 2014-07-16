#!/bin/bash
set -x
cat 0051-regexp.out | grep monkey|| exit 1
exit 0
