
FOP = fop
# FOP = fop/fop.sh

all: phps pdfs

phps: userguide.php tutorial.php tutorial-live.php quickstartguide.php reallyquickstartguide.php provenance.php historical/languagespec.php historical/languagespec-0.6.php log-processing.php plot-tour.php

htmls: userguide.html tutorial.html tutorial-live.html quickstartguide.html reallyquickstartguide.html provenance.html historical/languagespec.html historical/languagespec-0.6.html log-processing.html plot-tour.html

pdfs: userguide.pdf tutorial.pdf tutorial-live.pdf quickstartguide.pdf reallyquickstartguide.pdf provenance.pdf historical/languagespec.pdf historical/languagespec-0.6.pdf log-processing.pdf

GUIDE_PHP=$(shell find userguide -name "*.php" )
GUIDE_HTML=$(patsubst %.php,%.html,$(GUIDE_PHP))

guide_html: $(GUIDE_HTML)

chunked-userguide: userguide.xml
	./build-chunked-userguide.sh

%.php: %.xml formatting/swiftsh_html.xsl
	xsltproc --nonet formatting/swiftsh_html.xsl $<
	sed -e "s/index.html#/#/g" index.html >$@
	chmod a+rx $@

%.pdf: %.xml formatting/vdl2_fo.xsl
	$(FOP) -xsl formatting/vdl2_fo.xsl -xml $< -pdf $@
	chmod a+rx $@

%.html: %.php
	cp $< $@
	chmod a+rx $@

clean:
	rm -fv *.php userguide/*.php
