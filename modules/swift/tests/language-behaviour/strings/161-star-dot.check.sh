#!/bin/bash
set -x
cat 161-star-dot.out | grep 'two\-C'|| exit 1
exit 0
