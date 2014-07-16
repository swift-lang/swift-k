#!/bin/sh

set -x

mkdir -v 202

{
  uname -a
  date
} > 202/202-input-1.txt

{
  hostname
  pwd
} > 202/202-input-2.txt

exit 0
