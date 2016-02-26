#! /bin/sh

#/home/ketan/swift-0.95/cog/modules/swift/dist/swift-svn/bin/swift -sites.file cobalt.xml -config cf -tc.file apps namd.swift 
/home/ketan/swift-k/dist/swift-svn/bin/swift -config mira.conf -reducedLogging -minimalLogging namd.swift

#A testblock run
#qsub -A ATPESC2013 -n 32 -t 5 -q low --mode c16 --mode script testblock.sh # --disable_preboot
