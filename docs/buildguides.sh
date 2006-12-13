#!/bin/sh

process() {
	guide=$1
	xsl=$2
	out=${guide:0:${#guide}-4}.shtml
	xsltproc formatting/$xsl $guide
	#that's 'cause --output doesn't work
	sed -e "s/index.html#/#/" index.html >$out
}

process "userguide.xml" "vdl2sh_html.xsl"
process "quickstartguide.xml" "vdl2_html.xsl"
process "reallyquickstartguide.xml" "vdl2_html.xsl"