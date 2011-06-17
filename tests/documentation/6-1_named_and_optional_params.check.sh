#!/bin/bash
set -x
grep 'hello' english.txt | grep 'bonjour' french.txt || exit 1

exit 0
