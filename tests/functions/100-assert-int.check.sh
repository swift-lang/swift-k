#!/bin/bash

set -x

grep AssertFailedException stdout.txt || exit 1

exit 0
