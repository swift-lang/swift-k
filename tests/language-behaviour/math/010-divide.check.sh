#!/bin/bash
set -x
cat 010-divide.out | grep 33|| exit 1
exit 0
