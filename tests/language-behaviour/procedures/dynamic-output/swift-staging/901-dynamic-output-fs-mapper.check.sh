#!/bin/bash

cat 901-dynamic-output-fs-mapper.stdout |grep "SwiftScript trace: 3" || exit 1

exit 0