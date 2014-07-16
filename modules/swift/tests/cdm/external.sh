#!/bin/bash

set -x

{
  SRCDIR=$1
  SRCFILE=$2
  DESTHOST=$3
  DESTDIR=$4

  cp -v $SRCDIR/$SRCFILE $DESTDIR || exit 1

} > external.out 2>&1

exit 0
