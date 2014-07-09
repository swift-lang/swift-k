export BEAGLE_USERNAME="yadunandb"
export MIDWAY_USERNAME="yadunand"
export MCS_USERNAME="yadunand"
export UC3_USERNAME="yadunand"
export BLUES_USERNAME="yadunand"
export FUSION_USERNAME="yadunand"
export COMM_USERNAME="yadunandb"
export BRID_USERNAME="yadunandb"
export FROM_MAIL="Test-Engine@midway001"
export TO_MAIL="yadudoc1729@gmail.com davidkelly999@uchicago.edu wilde@mcs.anl.gov"
#export TO_MAIL="yadudoc1729@gmail.com"

################# MUST UPDATE FOR EACH REVISION #########################
COG_URL=https://svn.code.sf.net/p/cogkit/svn/branches/4.1.11/src/cog
SWIFT_URLk=https://svn.ci.uchicago.edu/svn/vdl2/branches/release-0.95
export SWIFT_TAR_FILE=""
export SWIFT_VERSION_OVERRIDE="swift-0.95"
export SWIFT_VERSION="swift-0.95"
################# MUST UPDATE FOR EACH REVISION #########################

export REMOTE_DRIVER_FASTSETUP="false"
#export REMOTE_DRIVER_FASTSETUP="true"
export CLEAN_CHECKOUT="yes"

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

export PATH=$PWD/tutorial/app:$PATH
