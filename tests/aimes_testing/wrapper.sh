#!/bin/bash


echo "Hello World!"
echo "HOSTNAME: $(hostname -f)"
echo "PWD: $PWD"

env >&2

# Creating a wrapper.error to convice radical-pilots that all the STAGE_OUT files
# were created
echo "" >> wrapper.error
