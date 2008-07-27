#!/bin/bash

rm -f external-mapper-args.args
swift external-mapper-args.swift
if [ "$?" != "0" ] ; then
  echo FAILED - SwiftScript did not complete successfully
  exit 1
fi

diff external-mapper-args.args external-mapper-args.args.expected

if [ "$?" != "0" ] ; then
  echo FAILED - external mapper received wrong arguments
  exit 2
fi
