# GROUPLIST definition to run all local tests

GROUPLIST=( # Site testing test-group
            $TESTDIR/sites/beagle      \
            $TESTDIR/sites/blacklight  \
            $TESTDIR/sites/blues       \
            $TESTDIR/sites/bridled     \
            $TESTDIR/sites/communicado \
            $TESTDIR/sites/ec2         \
            $TESTDIR/sites/fusion      \
            $TESTDIR/sites/gce         \
            $TESTDIR/sites/godzilla    \
            $TESTDIR/sites/local       \
            $TESTDIR/sites/local-coasters   \
            $TESTDIR/sites/mac-frisbee \
            $TESTDIR/sites/mcs         \
            $TESTDIR/sites/midway      \
            $TESTDIR/sites/multiple_coaster_pools  \
            $TESTDIR/sites/osgconnect  \
            $TESTDIR/sites/raven       \
            $TESTDIR/sites/ssh-cl-coasters  \

            # Stampede test does not work
            #$TESTDIR/sites/stampede  \
            $TESTDIR/sites/swan  \

          )

checkvars WORK
