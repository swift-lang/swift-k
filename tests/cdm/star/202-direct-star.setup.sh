#!/bin/sh

set -x

mkdir 201

{
  uname -a
  date
} > 201/201-input.txt

exit 0
