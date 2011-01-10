#!/bin/bash

# Sketch of meta script

# Runs nightly.sh on various sites

DIR=$1 # E.g., /home/wozniak/nightly-tests

ssh intrepid.alcf.anl.gov $DIR/run-nightly.sh groups/group-intrepid.sh

# Retrieve results
# scp ...

# Repeat for other sites...



# Build nice HTML index of results from all sites

return 0
