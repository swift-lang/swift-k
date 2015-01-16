export BEAGLE_USERNAME="yadunandb"
export MIDWAY_USERNAME="yadunand"
export MCS_USERNAME="yadunand"
export UC3_USERNAME="yadunand"
export OSGC_USERNAME="yadunand"
export BLUES_USERNAME="yadunand"
export FUSION_USERNAME="yadunand"
export COMM_USERNAME="yadunandb"
export BRID_USERNAME="yadunandb"
export SWAN_USERNAME="p01953"
export RAVEN_USERNAME="p01953"
export FROM_MAIL="Test-Engine@midway001"
export TO_MAIL="yadudoc1729@gmail.com hategan@mcs.anl.gov wilde@mcs.anl.gov"

#export REMOTE_DRIVER_FASTSETUP="false"
export REMOTE_DRIVER_FASTSETUP="true"
export SWIFT_TAR_FILE="/scratch/midway/yadunand/swift-trunk.tar"

export SWIFT_LOCAL_REPO="/scratch/midway/yadunand/swift-k"
export SWIFT_GIT_REPO="https://github.com/swift-lang/swift-k.git"
export CLEAN_CHECKOUT="yes"  # only yes or no
export KILL_JAVA="false"

# Extra performance stats
export COG_OPTS=-Dtcp.channel.log.io.performance=true

export RUN_TYPE="daily"
#export RUN_TYPE="test"

if [ "midway001" == "midway001" ]
then
   export GLOBUS_HOSTNAME=swift.rcc.uchicago.edu
   export GLOBUS_TCP_PORT_RANGE=50000,51000
fi;
