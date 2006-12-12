#!/bin/sh

for guide in *.xml; do
	out=${guide:0:${#guide}-4}.shtml
	xsltproc formatting/vdl2_html.xsl $guide
	#that's 'cause --output doesn't work
	sed -e "s/index.html#/#/" index.html >$out
done