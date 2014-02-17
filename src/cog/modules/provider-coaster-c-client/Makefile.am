ACLOCAL_AMFLAGS = -I m4
SUBDIRS = src 
EXTRA_DIST = autogen.sh
LIBS = -pthread $LIBS
AM_CXXFLAGS = -pthread

binaries: all
	mkdir -p dist/bin
	mkdir -p dist/lib
	cp src/.libs/libcoasterclient.a dist/lib
	cp -d src/.libs/libcoasterclient.so* dist/lib
	cp src/.libs/coaster-client-test dist/bin
	cp src/.libs/run-coaster-job dist/bin

if SWIG

SUBDIRS += tcl

endif
