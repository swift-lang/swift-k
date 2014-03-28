#!/bin/bash

cat 900-dynamic-output-simple-mapper.out |grep "SwiftScript trace: 3" || exit 1

exit 0