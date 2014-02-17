
if SWIG

tcldir = $(PWD)

tcl_DATA = pkgIndex.tcl

# This dependency points to the existence of a soft link
pkgIndex.tcl: libcoasterclient.so
	@echo Making Tcl package...
	tclsh ./make-package.tcl > pkgIndex.tcl

# This is a soft link
libcoasterclient.so: 
	ln -s ../src/.libs/libcoasterclient.so

endif
