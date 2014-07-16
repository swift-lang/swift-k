#!/bin/bash

# run tests with no retries
# this should expose, for example, the problem i'm seeing with
# 130-fmri failing once on my mac

export CF=swift.properties.no-retries

cd ../language-behaviour

swift -tc.file ./tc.data -dryrun 061-cattwo.swift
if [ "$?" -ne "0" ]; then echo "SWIFT RETURN CODE NON-ZERO"; exit 1; fi


swift -tc.file ./tc.data -dryrun 130-fmri.swift
if [ "$?" -ne "0" ]; then echo "SWIFT RETURN CODE NON-ZERO"; exit 1; fi

