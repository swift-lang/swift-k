#!/bin/sh

set -x

mkdir -pv 301 || exit 1

{
  uname -a
  date
} > 301-input.txt

exit 0
