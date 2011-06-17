#!/bin/bash
set -x
grep 'hello' english.txt | grep 'bonjour' francais.txt | grep 'konnichiwa' nihongo.txt || exit 1

exit 0
