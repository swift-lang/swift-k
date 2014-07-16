#!/bin/bash
set -x
cat 141-space-in-filename.space\ here.out | grep hello|| exit 1
exit 0
