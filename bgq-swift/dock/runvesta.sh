#! /bin/sh

#Old method
#qsub -A ATPESC2014 -n 512 -t 15 --mode script runswift_script.sh -config cf -tc.file apps -sites.file localcoast.xml "$@"
export COBALT_PROJ=ExM
#New method
/home/ketan/swift-0.95/cog/modules/swift/dist/swift-svn/bin/swift -sites.file cobalt.xml -config cf -tc.file apps dock.swift 

#A testblock run
#qsub -A ATPESC2013 -n 32 -t 5 -q low --mode c16 --mode script testblock.sh # --disable_preboot
