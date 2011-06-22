#!/bin/bash
set -x
cat 123-global-vars.out | grep hello|| exit 1
exit 0
