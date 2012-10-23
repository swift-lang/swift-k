#!/bin/sh

set -x

grep hello < hello.txt || exit 1

exit 0
