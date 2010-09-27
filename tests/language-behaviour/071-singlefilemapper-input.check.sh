#!/bin/bash

set -x

[ -f 071-singlefilemapper-input.in ] || exit 1
grep 071-singlefilemapper-input.in 071-singlefilemapper-input.out || exit 1

exit 0
