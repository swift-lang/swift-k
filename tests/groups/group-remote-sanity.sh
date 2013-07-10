
# GROUPLIST definition to run all local tests

GROUPLIST=( 
            $TESTDIR/stress/remote_sanity/beagle \
            $TESTDIR/stress/remote_sanity/mcs    \
            $TESTDIR/stress/remote_sanity/midway \
	    $TESTDIR/stress/remote_sanity/uc3    \
            $TESTDIR/stress/remote_sanity/mac-frisbee  \
          )

checkvars WORK
