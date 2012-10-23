#!/bin/bash

# run tests with no retries
# this should expose, for example, the problem i'm seeing with
# 130-fmri failing once on my mac

# export CF=swift.properties.no-retries

echo "localhost	helper	$(pwd)/path-prefix-helper	INSTALLED	INTEL32::LINUX	env::PATHPREFIX=$(pwd)" > tmp.path-prefix.tc.data

swift -tc.file tmp.path-prefix.tc.data path-prefix.swift

