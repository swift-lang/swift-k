#!/bin/bash
set -x
cat 005-strcut.out | grep def|| exit 1
exit 0
