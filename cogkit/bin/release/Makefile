#CVSROOT=:pserver:anonymous@cvs.cogkit.org:/cvs/cogkit
CVSROOT=https://svn.sourceforge.net/svnroot/cogkit
# for example TAG=-r 4_0_alpha1
TAG=now
CRTDIR=$(shell pwd)
LOG=$(CRTDIR)/log.txt
export LOG
export CVSROOT
#must be after the above
VERSION=$(shell release.sh cogversion $(CVSROOT) $(TAG))
EXCLUDEMODULES=qos common portlets template monitor provider-gt3_0_2 provider-gt3_2_0 broker provider-ws provider-mpichg2
PROVIDERS=gt4_0_0 webdav ssh condor gt2ft
#PUBLISHHOST=hategan@www.cogkit.org
PUBLISHHOST=dkodeboy@www.cogkit.org
#PUBLISHROOT=/www/www.cogkit/org/release
PUBLISHROOT=/www/www.cogkit.org/release
PUBLISHDIR=$(PUBLISHROOT)/$(VERSION)
URLROOT=http://www.cogkit.org/releases
OUTDIR=$(CRTDIR)/out
DISTNAME=cog-$(VERSION)
KARAJAN=cog-workflow


all:help

.SUFFIXES:

.PHONY: distclean dist-all dist-provider-% clean-cog clean guides cog release

#$!^&*^%!*&@#!
.SECONDARY:

help:
	@echo $(DISTNAME)
	@echo "Targets: "
	@echo
	@echo "release          makes a full release without publishing in $(PUBLISHROOT)/$(VERSION)"
	@echo
	@echo "publish          publishes a release on the server under $(PUBLISHROOT)/$(VERSION)"
	@echo
	@echo "lock             makes all release files in $(PUBLISHROOT)/$(VERSION) read-only"
	@echo
	@echo "cog              generates all cog packages"
	@echo
	@echo "cog-no-checkout  like cog, but it expects a checkout to exist in ./src/cog"
	@echo
	@echo "guides           generates all guides"
	@echo
	@echo "web              generates web pages"
	@echo
	@echo "clean            cleans all generated packages"
	@echo
	@echo "maven			makes a Maven repository of the abstractions module in $(PUBLISHROOT)/$(VERSION)/maven/"
	@echo "					Maven project files can be found in $(PUBLISHROOT)/$(VERSION)/maven/project-files"


release: print-info cog web

cog: clean-cog checkout-cog source-packs cog-all-bin-packs cog-main-bin-packs provider-bin-packs api-docs-html api-docs-packs

cog-no-checkout: source-packs cog-all-bin-packs cog-main-bin-packs provider-bin-packs api-docs-html api-docs-packs

cog-nc-na: source-packs cog-all-bin-packs cog-main-bin-packs provider-bin-packs

print-info:
	@echo Version: $(VERSION)
	@echo Distname: $(DISTNAME)

clean-cog:
	@echo Cleaning previous checkout
	@rm -rf src >>$(LOG)

clean:
	@echo Cleaning all output
	@rm -f out
	@rm -rf src/cog-compile
	@rm -f *.html
	@rm -f *.png
	@rm -f log.txt


$(OUTDIR):
	@mkdir -p $(OUTDIR)

checkout-cog: actual-checkout-cog remove-excluded

actual-checkout-cog:
	@release.sh checkout src/cog $(TAG)

remove-excluded:
	@echo Removing excluded modules
	@$(foreach exclude, $(EXCLUDEMODULES), rm -rf src/cog/modules/$(exclude))

#(*&$(*#@
source-packs:
	make $(OUTDIR)/$(DISTNAME)-src.tar.gz 
	make $(OUTDIR)/$(DISTNAME)-src.zip

$(OUTDIR)/$(DISTNAME)-src.tar.gz: $(OUTDIR)
	@echo Generating source tar.gz package
	@cd src; tar cf - cog | gzip -f9 > $(OUTDIR)/$(DISTNAME)-src.tar.gz 2>>$(LOG)

$(OUTDIR)/$(DISTNAME)-src.zip: $(OUTDIR)
	@echo Generating source zip package
	@cd src; zip -9 -r $(OUTDIR)/$(DISTNAME)-src.zip cog >>$(LOG)

cog-all-bin-packs: $(OUTDIR)/$(DISTNAME)-bin.tar.gz $(OUTDIR)/$(DISTNAME)-bin.zip

$(OUTDIR)/$(DISTNAME)-bin.tar.gz: $(OUTDIR) dist-all
	@echo Generating full binary tar.gz package
	@cd src/cog-compile/dist; tar cf - $(DISTNAME) | gzip -f9 > $(OUTDIR)/$(DISTNAME)-bin.tar.gz 2>>$(LOG)

$(OUTDIR)/$(DISTNAME)-bin.zip: $(OUTDIR) dist-all
	@echo Generating full binary zip package
	@cd src/cog-compile/dist; zip -9 -r $(OUTDIR)/$(DISTNAME)-bin.zip $(DISTNAME) >>$(LOG)

cog-main-bin-packs: $(OUTDIR)/$(DISTNAME)-main-bin.tar.gz $(OUTDIR)/$(DISTNAME)-main-bin.zip

$(OUTDIR)/$(DISTNAME)-main-bin.tar.gz: $(OUTDIR) dist-main
	@echo Generating binary tar.gz package with no providers
	@cd src/cog-compile/dist; tar cf - $(DISTNAME) | gzip -f9 > $(OUTDIR)/$(DISTNAME)-main-bin.tar.gz 2>>$(LOG)

$(OUTDIR)/$(DISTNAME)-main-bin.zip: $(OUTDIR) dist-main
	@echo Generating binary zip package with no providers
	@cd src/cog-compile/dist; zip -9 -r $(OUTDIR)/$(DISTNAME)-main-bin.zip $(DISTNAME) >>$(LOG)

provider-bin-packs: 
	@echo $(PROVIDERS) >providers
	@release.sh providers $(DISTNAME)
#$(foreach provider, $(PROVIDERS), $(DISTNAME)-provider-$(provider)-bin)

dist-all:
	@echo Building full CoG
	@rm -rf src/cog-compile
	@cp -R src/cog src/cog-compile >>$(LOG)
	@cd src/cog-compile; ant -quiet dist >>$(LOG)

dist-main:
	@echo Building CoG without providers
	@rm -rf src/cog-compile
	@cp -R src/cog src/cog-compile >>$(LOG)
	@cd src/cog-compile; ant -quiet dist -Dno.providers=true >>$(LOG)


$(DISTNAME)-provider-%-bin: pversion = $(shell release.sh providerversion $* $(CVSROOT) $(TAG))
$(DISTNAME)-provider-%-bin: $(OUTDIR)/$(DISTNAME)-provider-%-bin.tar.gz $(OUTDIR)/$(DISTNAME)-provider-%-bin.zip
	

dist-provider-%:
	@echo Building $* provider
	@rm -rf src/cog-compile
	@cp -R src/cog src/cog-compile >>$(LOG)
	@cd src/cog-compile/modules/provider-$*; ant -quiet module.package >>$(LOG)

$(OUTDIR)/$(DISTNAME)-provider-%-bin.tar.gz: $(OUTDIR) dist-provider-%
	@echo Generating $* provider tar.gz package
	@cd src/cog-compile/modules/provider-$*/dist/provider-$*-$(pversion)-package; \
		tar cf - $(DISTNAME) | gzip -f9 > $(OUTDIR)/$(DISTNAME)-provider-$*-bin.tar.gz 2>>$(LOG)

$(OUTDIR)/$(DISTNAME)-provider-%-bin.zip: $(OUTDIR) dist-provider-%
	@echo Generating $* provider zip package
	@cd src/cog-compile/modules/provider-$*/dist/provider-$*-$(pversion)-package; \
		zip -9 -r $(OUTDIR)/$(DISTNAME)-provider-$*-bin.zip $(DISTNAME) >>$(LOG)

api-docs:
	@echo Building javadocs
	@rm -rf src/cog-compile
	@cp -R src/cog src/cog-compile >>$(LOG)
	@cd src/cog-compile; ant -quiet javadoc >>$(LOG)

api-docs-html: $(OUTDIR) api-docs
	@echo Copying api docs to out
	@mkdir -p $(OUTDIR)/api
	@cp -r src/cog-compile/dist/$(DISTNAME)/javadoc/* $(OUTDIR)/api

api-docs-packs: $(OUTDIR)/$(DISTNAME)-api.tar.gz $(OUTDIR)/$(DISTNAME)-api.zip

$(OUTDIR)/$(DISTNAME)-api.tar.gz: $(OUTDIR) api-docs
	@echo Building javadoc tar.gz package
	@cd src/cog-compile/dist/$(DISTNAME)/javadoc; \
		tar cf - . | gzip -f9 > $(OUTDIR)/$(DISTNAME)-api.tar.gz 2>>$(LOG)

$(OUTDIR)/$(DISTNAME)-api.zip: $(OUTDIR) api-docs
	@echo Building javadoc zip package
	@cd src/cog-compile/dist/$(DISTNAME)/javadoc; \
		zip -9 -r $(OUTDIR)/$(DISTNAME)-api.zip . >>$(LOG)

guides: clean-guides checkout-guides compile-guides copy-guides

clean-guides:
	@echo Cleaning guides
	@rm -rf papers
	@rm -rf doc

checkout-guides: papers/bib doc/manual/guide doc/manual/tex

papers/bib:
	@release.sh checkout papers/bib $(TAG)

doc/manual/guide:
	@release.sh checkout doc/manual/guide $(TAG)

doc/manual/tex:
	@release.sh checkout doc/manual/tex $(TAG)

compile-guides:
	@echo Compiling guides
	@cd doc/manual/guide; make remote

copy-guides: $(OUTDIR)
	@echo Copying guides to release dir
	@cp -r doc/manual/guide/release/$(VERSION)/* $(OUTDIR)/

#web: index metaindex
web: metaindex

index: copy-index

compile-index:
	@echo Compiling index page
	@cd doc/manual/guide; make index

copy-index: $(OUTDIR)/index.css $(OUTDIR)/index.html

$(OUTDIR)/index.%: $(OUTDIR)
	@cp doc/manual/guide/index.$* $(OUTDIR)/

src/cog-compile/dist/$(DISTNAME)/bin/$(KARAJAN):

metaindex: src/cog-compile/dist/$(DISTNAME)/bin/$(KARAJAN) $(OUTDIR)
	@echo Generating module information
	@src/cog-compile/dist/$(DISTNAME)/bin/$(KARAJAN) metaindex.k >>$(LOG)
	@cp modules.html $(OUTDIR)/
	@cp *.png $(OUTDIR)/

publish:
	@echo Publishing files to $(PUBLISHDIR) on $(PUBLISHHOST)
	@release.sh publish $(PUBLISHHOST) $(PUBLISHDIR)

lock:
	@echo Locking release on $(PUBLISHDIR) on $(PUBLISHHOST)
	@release.sh lock $(PUBLISHHOST) $(PUBLISHDIR)

dist-abstractions:
	@echo Buliding abstractions...
	@rm -rf src/cog-compile
	@cp -R src/cog src/cog-compile >>$(LOG)
	@cd src/cog-compile/modules/abstraction; ant -quiet dist >>$(LOG)

maven: dist-abstractions repo

repo:
	@echo Building maven repository...
	@mkdir -p $(OUTDIR)/maven
	@setupmaven.sh $(OUTDIR)/maven $(URLROOT)/$(VERSION)/maven
