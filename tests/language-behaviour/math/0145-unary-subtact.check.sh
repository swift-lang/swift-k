#!/bin/bash
set -x
cat 0145-unary-subtact.out | grep '\-989929'|| exit 1
exit 0
