
# GROUPLIST definition to run all local tests

GROUPLIST=( # Remote sanity test-group
            $TESTDIR/stress/remote_sanity/beagle \
            $TESTDIR/stress/remote_sanity/mcs    \
            $TESTDIR/stress/remote_sanity/midway \
            $TESTDIR/stress/remote_sanity/uc3    \
	    # Frisbee will fail due to Bug 1030  
            #TESTDIR/stress/remote_sanity/mac-frisbee  \
	    
	    # Remote-cluster IO tests
	    $TESTDIR/stress/IO/beagle \
            $TESTDIR/stress/IO/bagOnodes \
            $TESTDIR/stress/IO/multiple \
            $TESTDIR/stress/IO/uc3 \

	    # Remote-cluster Apps tests - MODIS
            $TESTDIR/stress/apps/modis_beagle  \
            $TESTDIR/stress/apps/modis_local   \
	    $TESTDIR/stress/apps/modis_midway  \
	    $TESTDIR/stress/apps/modis_uc3     \
            $TESTDIR/stress/apps/modis_multiple\

	    # Recursive Test invocation 
	    $TESTDIR/multi-remote
	    
          )

checkvars WORK
