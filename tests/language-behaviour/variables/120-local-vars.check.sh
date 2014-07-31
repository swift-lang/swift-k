#!/bin/bash
set -x
cat 120-local-vars.out | grep hi|| exit 1
exit 0
