#!/bin/bash
set -x
cat 0054-strsplit.out | grep 'ab , c , def , ghij' || exit 1
exit 0
