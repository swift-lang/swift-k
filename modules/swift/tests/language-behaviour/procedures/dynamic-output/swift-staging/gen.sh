#!/bin/bash

mkdir outs
for ((i = 0; i < $1; i++)); do
	echo "test" > outs/foo000$i.out
done