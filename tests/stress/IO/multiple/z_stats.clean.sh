#!/bin/bash

echo "Driver output files to be cleaned up : "
ls driver[0-9][0-9][0-9][0-9]*out -thor

rm driver[0-9][0-9][0-9][0-9]*out
