#!/bin/bash
set -x
cat q5out.txt|grep 'how are you'|| exit 1
exit 0
