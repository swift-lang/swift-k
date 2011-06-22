#!/bin/bash
set -x
cat 111-array-individual-assigns | grep 100 || exit 1
exit 0
