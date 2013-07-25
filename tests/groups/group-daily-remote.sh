
# GROUPLIST definition to run all local tests

GROUPLIST=( # Site testing test-group
            $TESTDIR/sites/beagle \
            $TESTDIR/sites/mcs    \
            $TESTDIR/sites/midway \
            $TESTDIR/sites/uc3    \
	    # Frisbee will fail due to Bug 1030  
            #TESTDIR/sites/mac-frisbee  \
	    
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
