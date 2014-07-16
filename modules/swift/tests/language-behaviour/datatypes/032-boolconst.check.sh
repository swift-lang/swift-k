#!/bin/bash
set -x
cat 032-boolconst.f.out | grep 'false' || exit 1
cat 032-boolconst.t.out | grep 'true' || exit 1
exit 0
