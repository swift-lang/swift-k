#!/bin/bash

set -x

grep ASSERT_MESSAGE stdout.txt || exit 1

exit 0
