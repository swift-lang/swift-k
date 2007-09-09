#!/bin/sh

html() {
	guide=$1
	xsl=$2
	out=${guide:0:${#guide}-4}.php
	xsltproc formatting/$xsl $guide
	#that's 'cause --output doesn't work
	sed -e "s/index.html#/#/g" index.html >$out
}

pdf() {
	guide=$1
	xsl=$2
	out=${guide:0:${#guide}-4}.pdf
	fop/fop.sh -xsl formatting/vdl2_fo.xsl -xml $guide -pdf $out
}

process() {
	html $1 $2
	pdf $1
}



process "userguide.xml" "swiftsh_html.xsl"
process "tutorial.xml" "swiftsh_html.xsl"
process "tutorial-live.xml" "swiftsh_html.xsl"
process "quickstartguide.xml" "swift_html.xsl"
process "reallyquickstartguide.xml" "swift_html.xsl"
process "languagespec.xml" "swift_html.xsl"
process "languagespec-0.6.xml" "swift_html.xsl"
