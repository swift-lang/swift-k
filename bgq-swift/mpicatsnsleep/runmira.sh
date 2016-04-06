#! /bin/sh

#Old method
#qsub -A ATPESC2014 -n 512 -t 15 --mode script runswift_script.sh -config cf -tc.file apps -sites.file localcoast.xml "$@"

#build app
#mpixlc mpicatnap.c -o mpicatnap

#New method
#/home/ketan/swift-0.95/cog/modules/swift/dist/swift-svn/bin/swift -sites.file cobalt.cetus.xml -config cf -tc.file apps catsnsleepmpi.swift -n=8 -s=1
/home/ketan/swift-k/dist/swift-svn/bin/swift -config mira.conf -reducedLogging -minimalLogging catsnsleepmpi.swift -n=400 -s=1

#A testblock run
#qsub -A ATPESC2013 -n 32 -t 5 -q low --mode c16 --mode script testblock.sh # --disable_preboot
