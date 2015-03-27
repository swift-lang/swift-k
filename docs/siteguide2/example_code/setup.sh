######################### CONFIGS ###########################

export BEAGLE_USERNAME=""
export BEAGLE_PROJECT=""
export MIDWAY_USERNAME=""
export AWS_CREDENTIALS_FILE=""
export URL_OF_AD_HOC_MACHINE_1=""
export AD_HOC_1_USERNAME=""
export AD_HOC_N_USERNAME=""
export OSG_USERNAME=""
export OSG_PROJECT=""
export BLUES_USERNAME=""

#############################################################


# ensure that this script is being sourced
if [ ${BASH_VERSINFO[0]} -gt 2 -a "${BASH_SOURCE[0]}" = "${0}" ] ; then
  echo ERROR: script ${BASH_SOURCE[0]} must be executed as: source ${BASH_SOURCE[0]}
  exit 1
fi


# Setting scripts folder to the PATH env var.
TUTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ _$(which cleanup 2>/dev/null) != _$TUTDIR/bin/cleanup ]; then
  echo Adding $TUTDIR/bin:$TUTDIR/app: to front of PATH
  PATH=$TUTDIR/bin:$TUTDIR/app:$PATH
else
  echo Assuming $TUTDIR/bin:$TUTDIR/app: is already at front of PATH
fi

if [ -d /usr/local/bin/swift-trunk ] && [ -d /usr/local/bin/jdk1.7.0_51 ]
then
    export JAVA=/usr/local/bin/jdk1.7.0_51/bin
    export SWIFT=/usr/local/bin/swift-trunk/bin
    export PATH=$JAVA:$SWIFT:$PATH
fi

echo Swift version is $(swift -version)

return

