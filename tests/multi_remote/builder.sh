#!/bin/bash

COG_URL=https://cogkit.svn.sourceforge.net/svnroot/cogkit/branches/4.1.10/src/cog 
SWIFT_URL=https://svn.ci.uchicago.edu/svn/vdl2/branches/release-0.94
SWIFT_VERSION=0.94

BASE=$PWD

# Make clean checkout if no swift dir is present or
# Clean checkout requested
if [ "$1" == "clean" ] || [ ! -d "swift" ]
then
    echo "Cleaning and making fresh checkout"
    rm -rf swift &> /dev/null
    mkdir swift && cd swift
    svn co $COG_URL   
    cd cog/modules
    svn co $SWIFT_URL swift
    cd swift
else
    echo "Updating Cog sources"
    cd swift/
#    svn up *
    echo "Updating Swift sources"
    cd cog/modules/swift
#    svn up *    
fi

echo "$PWD : Starting compile"
ant redist | tee $BASE/compile.log
if [ "$?" != "0" ]
then
    echo "Swift compile failed. Cannot proceed"
    exit 1
fi

cd $BASE
if [ -d "swift" ]
then
    tar -cf swift.tar.tmp ./swift && mv swift.tar.tmp swift.tar && echo "Tarred successfully"
else
    echo "Could not find swift folder to tar"
fi;
exit 0