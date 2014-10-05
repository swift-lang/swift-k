#!/bin/bash

if [ -z $1 ]
then
    echo "This script expects an input file as first argument"
fi

# We first strip out all punctuation to make things easier
# Then some sed magic to get the words into tokens  [Source link:]
# (http://stackoverflow.com/questions/10552803/how-to-create-a-frequency-list-of-every-word-in-a-file)
# piped to sort and then count unique words
cat $1 | sed -e 's/\([[:punct:]]\)//g' | sed 's/\.//g;s/\(.*\)/\L\1/;s/\ /\n/g' | sort | uniq -c | tr -d $'\r'

