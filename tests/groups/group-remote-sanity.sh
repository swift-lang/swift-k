# GROUPLIST definition to run all local tests

GROUPLIST=( # Site testing test-group
            $TESTDIR/sites/beagle \
            $TESTDIR/sites/mcs    \
            $TESTDIR/sites/midway \
            $TESTDIR/sites/uc3    \
	        # Frisbee will fail due to Bug 1030
            $TESTDIR/sites/mac-frisbee  \
            $TESTDIR/sites/blues  \
            $TESTDIR/sites/fusion \
            $TESTDIR/sites/raven  \
            $TESTDIR/sites/communicado \
            $TESTDIR/sites/bridled \
          )

checkvars WORK
