
# Run "tclsh make-package.tcl > pkgIndex.tcl" to create the package

set name     "coasters"
set version  "0.1"
set leaf_so  "libcoasterclient.so"
set leaf_tcl "coaster.tcl"

puts [ ::pkg::create -name $name -version $version \
           -load $leaf_so -source $leaf_tcl ]
