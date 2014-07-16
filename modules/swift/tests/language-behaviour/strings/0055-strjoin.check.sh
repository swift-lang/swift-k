#!/bin/bash

set -x
filename="0055-strjoin.stdout"

grep -x "Empty: " $filename || exit 1
grep -x "Integer array: 1:2:3:4:5" $filename || exit 1
grep -x "String array: a:bb:ccc" $filename || exit 1
grep -x "Boolean array: true:false:true:false" $filename || exit 1
grep -x "Float array: 1.1:2.2:3.3:4.4:5.5" $filename || exit 1

exit 0
