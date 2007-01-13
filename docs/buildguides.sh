#!/bin/sh

html() {
	guide=$1
	xsl=$2
	out=${guide:0:${#guide}-4}.shtml
	xsltproc formatting/$xsl $guide
	#that's 'cause --output doesn't work
	sed -e "s/index.html#/#/g" index.html >$out
}

pdf() {
	guide=$1
	xsl=$2
	out=${guide:0:${#guide}-4}.pdf
	fop/fop.sh -xsl formatting/custom_fo.xsl -xml $guide -pdf $out
}

process() {
	html $1 $2
	pdf $1
}



process "userguide.xml" "vdl2sh_html.xsl"
process "tutorial.xml" "vdl2sh_html.xsl"
process "quickstartguide.xml" "vdl2_html.xsl"
process "reallyquickstartguide.xml" "vdl2_html.xsl"
process "languagespec.xml" "vdl2_html.xsl"
