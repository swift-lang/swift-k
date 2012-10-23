#!/bin/bash
set -x
cat 027-array-assignment.out | grep two || exit 1
exit 0
