#!/bin/bash
set -x
cat 008-add-multiply.out | grep 8|| exit 1
exit 0
