#!/bin/bash


IN=0;
OUT=0;

for file in $*
do
    IN=$(( $IN + $(grep "YES" $file | wc -l) ))
    OUT=$(( $OUT + $(grep "NO" $file | wc -l) ))
done

echo "TOTAL IN : $IN"
echo "TOTAL OUT: $OUT"
# PI = (IN * 4)/ (IN+OUT)
echo -n "PI :"
echo "scale=10; ($IN * 4)/($IN+$OUT)" | bc