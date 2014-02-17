
# Run "tclsh make-package.tcl > pkgIndex.tcl" to create the package

set name     "coaster"
set version  "0.0"
set leaf_so  "libcoasterclient.so"
set leaf_tcl "coaster.tcl"

puts [ ::pkg::create -name $name -version $version \
           -load $leaf_so -source $leaf_tcl ]
