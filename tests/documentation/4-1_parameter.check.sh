#!/bin/bash
set -x
grep 'hello world' hello2.txt || exit 1
exit 0
