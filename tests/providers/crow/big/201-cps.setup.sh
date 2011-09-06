#!/bin/sh

set -x

{
  uname -a
  date
} > 201-input-1.txt

cp -v 201-input-1.txt 201-input-2.txt

exit 0
