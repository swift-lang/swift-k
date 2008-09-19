#!/bin/sh

mkdir -p userguide/ || exit 1
cd userguide/ || exit 2
rm -f *.html *.php

xsltproc --nonet ../formatting/swiftsh_html_chunked.xsl ../userguide.xml

for a in *.html; do
  B=$(basename -s .html $a)
  mv $a ${B}.php
done
