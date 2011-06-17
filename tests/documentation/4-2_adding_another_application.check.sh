#!/bin/bash
set -x
grep 'hello from Swift' hello.txt | grep 'HELLO FROM SWIFT' capitals.txt || exit 1

exit 0
