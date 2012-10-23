#!/bin/bash
set -x
cat 0031-strcat-op.out | grep 'abc,qux'|| exit 1
exit 0
