#!/bin/bash
set -x
cat -v 0231-complex-type.out | grep '3 44' || exit 1
exit 0
