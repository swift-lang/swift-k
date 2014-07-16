#!/bin/bash
set -x
cat 103-quote.out | grep '\"'|| exit 1
exit 0
