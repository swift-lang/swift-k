#!/bin/bash
set -x
cat 015-lesseq.out | grep true|| exit 1
exit 0
