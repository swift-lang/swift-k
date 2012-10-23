#!/bin/bash
set -x
cat 143-newlines.out | grep 'hello world'|| exit 1
exit 0
