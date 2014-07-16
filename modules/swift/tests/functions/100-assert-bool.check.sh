#!/bin/bash

set -x

grep ASSERT_MESSAGE 100-assert-bool.stdout || exit 1

exit 0
