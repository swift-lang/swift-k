#!/bin/sh

set -x

grep "hello world" < hw1.txt || exit 1
grep "hello again" < hw2.txt || exit 1

exit 0
