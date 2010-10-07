#!/bin/sh

set -x

{
  uname -a
  date
} > 210-input.txt

cp -v $GROUP/external.sh .

exit 0
