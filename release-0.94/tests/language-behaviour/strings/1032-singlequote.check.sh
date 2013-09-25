#!/bin/bash
set -x
cat 1032-singlequote.out | grep testing\ \'quotes\'\ in\ swift|| exit 1
exit 0
