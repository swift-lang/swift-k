#!/bin/bash
set -x
cat 1033-singlequote.out | grep \'|| exit 1
exit 0
