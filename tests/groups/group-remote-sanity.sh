# GROUPLIST definition to run all local tests

GROUPLIST=( # Site testing test-group
            $TESTDIR/sites/beagle \
            $TESTDIR/sites/mcs    \
            $TESTDIR/sites/midway \
            $TESTDIR/sites/uc3    \
            $TESTDIR/sites/mac-frisbee  \
            $TESTDIR/sites/blues  \
            $TESTDIR/sites/fusion \
            # Raven will fail due to firewall restrictions, need to set up a reverse tunnel
            # $TESTDIR/sites/raven  \
            $TESTDIR/sites/communicado \
            $TESTDIR/sites/bridled \
          )

GROUPLIST=( $TESTDIR/sites/communicado \
            $TESTDIR/sites/bridled \
          )
checkvars WORK
