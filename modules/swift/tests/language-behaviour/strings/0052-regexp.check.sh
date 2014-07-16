#!/bin/bash
set -x
cat 0052-regexp.out | grep abmonkeyhi || exit 1
exit 0
