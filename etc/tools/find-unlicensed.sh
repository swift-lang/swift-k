#!/bin/bash
set -e

if [ $# > 0 ]
then
  dir=$1
else
  dir=modules
fi

find $dir -type f | (
  set -e
  while read file
  do
    if [[ $file =~ .*\.class ]] || \
       [[ $file =~ .*\.jar ]] || \
       [[ $file =~ .*\.lo ]] || \
       [[ $file =~ .*\.o ]] || \
       [[ $file =~ .*\.Plo ]] || \
       [[ $file =~ .*\.Po ]] || \
       [[ $file =~ .*\.la ]] || \
       [[ $file =~ .*\.jpg ]] || \
       [[ $file =~ .*\.png ]] || \
       [[ $file =~ .*\.min.js ]] || \
       [[ $file =~ .*\.params ]] || \
       [[ $file =~ .*/\.settings/.* ]] || \
       [[ $file =~ .*/\.project ]]
    then
      # Compiled files dont need license
      :
    elif grep -q "is developed as part of the Java CoG Kit" $file
    then
      # It has a license
      :
    elif grep -q "Copyright .* University of Chicago" $file
    then
      # It has an up-to-date license
      :
    else
      echo $file
    fi
  done
)
