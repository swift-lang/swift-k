#!/bin/bash

# run tests with java assertions enabled

export COG_OPTS="-enableassertions"

cd ../language-behaviour

./run
