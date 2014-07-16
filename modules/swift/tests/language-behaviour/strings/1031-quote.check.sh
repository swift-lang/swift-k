#!/bin/bash
set -x
cat 1031-quote.out | grep 'testing \"quotes\" in swift'|| exit 1
exit 0
