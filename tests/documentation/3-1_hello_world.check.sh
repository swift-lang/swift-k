#!/bin/bash
set -x
grep 'Hello, world!' hello.txt || exit 1

exit 0
