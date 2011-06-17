#!/bin/bash
set -x
grep 'Hello. Your name is John and you have eaten 3 pies.' q15.txt || exit 1
exit 0
