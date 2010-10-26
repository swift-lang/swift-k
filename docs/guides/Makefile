#!/bin/sh

all: phps pdfs

phps: userguide.php tutorial.php tutorial-live.php quickstartguide.php reallyquickstartguide.php provenance.php historical/languagespec.php historical/languagespec-0.6.php log-processing.php plot-tour.php

htmls: userguide.html tutorial.html tutorial-live.html quickstartguide.html reallyquickstartguide.html provenance.html historical/languagespec.html historical/languagespec-0.6.html log-processing.html plot-tour.html

pdfs: userguide.pdf tutorial.pdf tutorial-live.pdf quickstartguide.pdf reallyquickstartguide.pdf provenance.pdf historical/languagespec.pdf historical/languagespec-0.6.pdf log-processing.pdf

chunked-userguide: userguide.xml
	./build-chunked-userguide.sh

%.php: %.xml formatting/swiftsh_html.xsl
	xsltproc --nonet formatting/swiftsh_html.xsl $<
	sed -e "s/index.html#/#/g" index.html >$@

%.pdf: %.xml formatting/vdl2_fo.xsl
	fop/fop.sh -xsl formatting/vdl2_fo.xsl -xml $< -pdf $@

%.html: %.php
	cp $< $@
