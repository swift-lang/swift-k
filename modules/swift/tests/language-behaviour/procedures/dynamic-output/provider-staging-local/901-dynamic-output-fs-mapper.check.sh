#!/bin/bash

cat 901-dynamic-output-fs-mapper.out |grep "SwiftScript trace: 3" || exit 1

exit 0