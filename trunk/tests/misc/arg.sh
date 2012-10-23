#!/bin/bash

swift arg-default.swift -foo=bar

if [ "$?" != "0" ]; then
 echo default arg test FAIL
 exit 1
fi

swift arg-nodefault.swift -foo=bar

if [ "$?" != "0" ]; then
 echo no-default arg test FAIL
 exit 2
fi

echo SUCCESS for arg tests
