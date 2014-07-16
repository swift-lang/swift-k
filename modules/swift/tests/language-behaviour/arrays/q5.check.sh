#!/bin/bash
set -x
cat q5.out | grep 'how are you'|| exit 1
exit 0
