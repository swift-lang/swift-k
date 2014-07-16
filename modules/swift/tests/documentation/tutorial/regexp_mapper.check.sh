#!/bin/bash

set -x

grep "5 regexp_mapper.words.txt" regexp_mapper.words.count || exit 1

exit 0
