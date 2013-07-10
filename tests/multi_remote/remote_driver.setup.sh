#!/bin/bash

#CLEAN_CHECKOUT="yes"
CLEAN_CHECKOUT="no"

[ ! -z $COG_URL ]         || COG_URL=https://cogkit.svn.sourceforge.net/svnroot/cogkit/branches/4.1.10/src/cog
[ ! -z $SWIFT_URL ]       || SWIFT_URL=https://svn.ci.uchicago.edu/svn/vdl2/branches/release-0.94
[ ! -z $SWIFT_VERSION ]   || SWIFT_VERSION=0.94
[ ! -z $BEAGLE_USERNAME ] || BEAGLE_USERNAME="yadunandb"
[ ! -z $MIDWAY_USERNAME ] || MIDWAY_USERNAME="yadunand"
[ ! -z $UC3_USERNAME ]    || UC3_USERNAME="yadunand"
[ ! -z $MCS_USERNAME ]    || MCS_USERNAME="yadunand"    


export GLOBUS_HOSTNAME="swift.rcc.uchicago.edu"


SITES="sites.xml"

cp  $SITES  $SITES.bak
cat $SITES | sed "s/BEAGLE_USERNAME/$BEAGLE_USERNAME/g" > tmp && mv tmp $SITES
cat $SITES | sed "s/MIDWAY_USERNAME/$MIDWAY_USERNAME/g" > tmp && mv tmp $SITES
cat $SITES | sed "s/UC3_USERNAME/$UC3_USERNAME/g"       > tmp && mv tmp $SITES
cat $SITES | sed "s/MCS_USERNAME/$MCS_USERNAME/g"       > tmp && mv tmp $SITES

BASE=$PWD
# Make clean checkout if no swift dir is present or                                        
# Clean checkout requested                                                               
cp /home/yadunand/swift/cog/modules/swift/tests/multi_remote/swift.tar ./
if [ -f "swift.tar" ]
then
    echo "Found swift.tar. Extracting.."
    tar -xf swift.tar
fi
  
if [ "CLEAN_CHECKOUT" == "yes" ] || [ ! -d "swift" ]
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
    svn up *                                                                        
    echo "Updating Swift sources"
    cd cog/modules/swift
    svn up *                                                                              
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


# Wrapper is the script that gets executed on the remote nodes
# The outputs go to the out directory
cat <<'EOF' > wrapper.sh

#!/bin/bash

SWIFT_TARBALL=$1
LOG_TARBALL=$2
RUN_HOME=$PWD
echo "Wrapper running on : $HOSTNAME "

if [ -f $SWIFT_TARBALL ]
then
    echo "Hey this is wrapper and the $1 exists as a file";
    ls -lah;
else
    echo "Doinks! the file we need isn't here";
    ls -lah
    exit -1;
fi

BASENAME=""
if echo $SWIFT_TARBALL | grep "\.tar$"
then
    tar -xf $SWIFT_TARBALL
    BASENAME=${SWIFT_TARBALL%.tar}
else
    echo "BAD.. cannot decipher $SWIFT_TARBALL"
    exit -1
fi;

cd $BASENAME;
cd cog/modules/swift/

#type ant   2>&1
#if [ "$?" != "0" ]
#then
#    echo "Ant not found. Cannot build. Exiting!.."
#    exit 0
#fi

#ant redist 2>&1 > tee $RUN_HOME/swift_build.log
if [ ! -x "$PWD/dist/swift-svn/bin/swift" ] 
then
    echo "No executable swift binary... Cannot proceed"
    exit 0
fi

echo "Found swift executable!"
SWIFT_PATH=$PWD/dist/swift-svn/bin
export PATH=$SWIFT_PATH:$PATH

T=`which swift`
if [ "$T" == "$SWIFT_PATH/swift" ]
then
    swift -version
else
    echo "Swift not being pulled from SWIFT_PATH"
fi;

cd tests/
#./suite.sh -l 1 -t $PWD/groups/group-stress-heavy.sh 2>&1 | tee $RUN_HOME/TEST.log
./suite.sh -l 1 -t $PWD/groups/group-functions.sh 2>&1 | tee $RUN_HOME/TEST.log
#./suite.sh -l 1 -t $PWD/groups/group-all-local.sh 2>&1 | tee $RUN_HOME/TEST.log

cd $RUN_HOME
tar -cvf $LOG_TARBALL *log $BASENAME/run*/tests-*{html,log}
rm $BASENAME* -rf
echo "Folder cleaned"
ls -lah
EOF

exit 0
