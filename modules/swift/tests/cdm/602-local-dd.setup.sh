#!/bin/sh

set -x

mkdir -pv 602 || exit 1

{
  uname -a
  date
} > 602/602-input.txt

exit 0
