export BEAGLE_USERNAME=""
export MIDWAY_USERNAME=""
export MCS_USERNAME=""
export UC3_USERNAME=""
export FROM_MAIL="test_engine@midway001"
export TO_MAIL=""
export KILL_JAVA="true" #Set to true to kill dead java processes

# FASTSETUP skips svn updates and rebuild to speed up multi_remote.setup.sh | Can be "true" or "false"
export REMOTE_DRIVER_FASTSETUP="false"
#export SWIFT_TAR_FILE="path/to/source/tarball/"

# Determines the groups of tests run on the remote nodes | and future behavior
# RUN_TYPE can be "daily", "weekly", "manual" (manual is undefined now)
export RUN_TYPE="daily" 

if [ "$HOSTNAME" == "midway001" ]
then
   export GLOBUS_HOSTNAME=swift.rcc.uchicago.edu
   export GLOBUS_TCP_PORT_RANGE=50000,51000
fi;
