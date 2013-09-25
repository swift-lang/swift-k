#!/bin/bash
set -x
cat 003-strcat.out | grep abcqux || exit 1
exit 0
