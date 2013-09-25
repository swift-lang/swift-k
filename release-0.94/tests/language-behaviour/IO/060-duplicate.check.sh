#!/bin/bash

cat 060-duplicate.out | grep 060-duplicate.in || exit 1

exit 0
