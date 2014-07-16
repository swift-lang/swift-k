#!/bin/sh

set -x

mkdir -pv 601 || exit 1

{
  uname -a
  date
} > 601/601-input.txt

exit 0
