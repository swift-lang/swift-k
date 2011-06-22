#!/bin/bash
set -x
grep array_multidemnsional_index.out | grep 'left up down' || exit 1
exit 0
