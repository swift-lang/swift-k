#!/bin/bash
set -x
cat 145-url.out | grep 'hello' || exit 1
exit 0
