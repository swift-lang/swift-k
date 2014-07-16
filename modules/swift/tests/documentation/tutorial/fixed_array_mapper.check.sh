#!/bin/bash

set -x

grep "1 fixed_array_mapper.1.txt" fixed_array_mapper.1.count || exit 1
grep "2 fixed_array_mapper.2.txt" fixed_array_mapper.2.count || exit 1
grep "3 fixed_array_mapper.3.txt" fixed_array_mapper.3.count || exit 1

exit 0
