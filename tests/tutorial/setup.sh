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

if [[ $HOSTNAME == headnode* ]]
then
    export JAVA=/usr/local/bin/jdk1.7.0_51/bin
    export SWIFT=/usr/local/bin/swift-0.95/bin
    export PATH=$JAVA:$SWIFT:$PATH
fi

echo Swift version is $(swift -version)

return

