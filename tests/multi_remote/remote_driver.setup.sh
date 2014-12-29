#!/bin/bash
[ ! -z $SWIFT_VERSION ]   || SWIFT_VERSION=trunk
[ ! -z $GIT_REPO ]        || GIT_REPO="https://github.com/swift-lang/swift-k.git"
[ ! -z $BEAGLE_USERNAME ] || BEAGLE_USERNAME="yadunandb"
[ ! -z $MIDWAY_USERNAME ] || MIDWAY_USERNAME="yadunand"
[ ! -z $UC3_USERNAME ]    || UC3_USERNAME="yadunand"
[ ! -z $MCS_USERNAME ]    || MCS_USERNAME="yadunand"
[ ! -z $FUSION_USERNAME ] || FUSION_USERNAME="yadunand"
[ ! -z $BLUES_USERNAME ]  || BLUES_USERNAME="yadunand"
[ ! -z $BRID_USERNAME ]   || BRID_USERNAME="yadunandb" # Bridled
[ ! -z $COMM_USERNAME ]   || COMM_USERNAME="yadunandb" # Communicado
[ ! -z $PUBLISH_FOLDER ]  || PUBLISH_FOLDER="\/home\/yadunandb\/public_html\/results"
[ ! -z $SWIFT_SOURCE ]    || SWIFT_SOURCE="/home/yadunand/swift"
[ ! -z $RUN_TYPE ]        || RUN_TYPE="daily"
[ ! -z $SWIFT_TAR_FILE ]  || SWIFT_TAR_FILE="/scratch/midway/yadunand/swift-trunk.tar"
[ ! -z $CLEAN_CHECKOUT ]  || CLEAN_CHECKOUT="true"

export GLOBUS_HOSTNAME="swift.rcc.uchicago.edu"

BASE=$PWD
# Make clean checkout if no swift dir is present or
# Clean checkout requested

[ -f "$SWIFT_TAR_FILE" ] && cp $SWIFT_TAR_FILE ./swift.tar

if [ "$REMOTE_DRIVER_FASTSETUP" == "true" ]
then
    echo "FASTSETUP: Skipping git update and rebuild"
else

    if [ "$CLEAN_CHECKOUT" == "true" ]
    then
	    echo "Cleaning and making fresh checkout"
	    rm -rf swift &> /dev/null
        git clone $GIT_REPO swift
	    cd swift
    else
        echo "CLEAN_CHECKOUT not enabled. Cannot proceed"
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
fi

# Wrapper is the script that gets executed on the remote nodes
# The outputs go to the out directory
cat <<'EOF' > wrapper.sh

#!/bin/bash

SWIFT_TARBALL=$1
LOG_TARBALL=$2
RUN_HOME=$PWD
echo "HOSTNAME: $HOSTNAME"

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

EOF


if   [ "$RUN_TYPE" == "daily" ]; then
cat <<'EOF' >> wrapper.sh
#./suite.sh -l 1 -t $PWD/groups/group-functions.sh 2>&1 | tee $RUN_HOME/TEST.log
#./suite.sh -l 1 -t $PWD/groups/group-remote-site.sh 2>&1 | tee $RUN_HOME/TEST.log
./suite.sh -l 1 -t $PWD/groups/group-all-local.sh 2>&1 | tee $RUN_HOME/TEST.log
EOF
elif [ "$RUN_TYPE" == "weekly" ]; then
cat <<'EOF' >> wrapper.sh
#./suite.sh -l 1 -t $PWD/groups/group-functions.sh 2>&1 | tee $RUN_HOME/TEST.log
#./suite.sh -l 1 -t $PWD/groups/group-remote-site.sh 2>&1 | tee $RUN_HOME/TEST.log
./suite.sh -l 1 -t $PWD/groups/group-all-local.sh 2>&1 | tee $RUN_HOME/TEST.log
EOF
elif [ "$RUN_TYPE" == "manual" ]; then
cat <<'EOF' >> wrapper.sh
#./suite.sh -l 1 -t $PWD/groups/group-functions.sh 2>&1 | tee $RUN_HOME/TEST.log
#./suite.sh -l 1 -t $PWD/groups/group-remote-site.sh 2>&1 | tee $RUN_HOME/TEST.log
./suite.sh -l 1 -t $PWD/groups/group-all-local.sh 2>&1 | tee $RUN_HOME/TEST.log
EOF
elif [ "$RUN_TYPE" == "test" ]; then
cat <<'EOF' >> wrapper.sh
./suite.sh -l 1 -t $PWD/groups/group-functions.sh 2>&1 | tee $RUN_HOME/TEST.log
EOF
fi

cat <<'EOF' >> wrapper.sh

cd $RUN_HOME
echo "Current dir : $PWD"
rm -rf $BASENAME/cog

RUNDIR=`ls $BASENAME | grep run`

cp *log $BASENAME/$RUNDIR/

mv $BASENAME/$RUNDIR "$RUNDIR-$HOSTNAME"
tar -cvf $LOG_TARBALL "$RUNDIR-$HOSTNAME" &> /dev/null
rm -rf $BASENAME* "$RUNDIR-$HOSTNAME"     &> /dev/null
echo "Folder cleaned"

PID=$$
CPIDS=$(pgrep -P $PID); (sleep 20 && kill -SIGKILL $CPIDS &); kill -TERM $CPIDS;
exit 0

EOF


cat<<'EOF' > publish.sh
#!/bin/bash
echo "Hostname : $HOSTNAME"
echo "PWD      : $PWD"
ls

EOF

echo "$0 : Completed"
exit 0
