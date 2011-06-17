#!/bin/bash
set -x
cp -v $GROUP/one.txt $GROUP/two.txt $GROUP/three.txt .|| exit 1
exit 0
