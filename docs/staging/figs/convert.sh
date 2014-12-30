#!/bin/bash

for i in $(echo *pdf)
do
    pdfcrop --margin '5 10 10 20' $i _$i
    convert -resize 800x200 -density 100 _$i ${i%.pdf}.png ;
done
