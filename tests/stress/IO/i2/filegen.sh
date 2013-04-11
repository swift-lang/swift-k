#!/bin/bash

case  $STRESS in
    "S1")
        FILES=100
	SIZE=102400
        ;;
    "S2")
        FILES=1000
	SIZE=102400
        ;;
    "S3")
        FILES=1000
	SIZE=1024000
        ;;
    "S4")
        FILES=10000
	SIZE=1024000
        ;;
    *)
	SIZE=102400
        FILES=1000
        ;;
esac

rm input/* -rf
for (( i=1; i <= $FILES ; i=$(($i+1)) ))
do
    top=$SIZE
    ./gendata.pl $top > input/file_$i.inp
done;