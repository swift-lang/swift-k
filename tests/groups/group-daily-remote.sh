# GROUPLIST definition to run all local tests

GROUPLIST=( $TESTDIR/language/working \
            $TESTDIR/local \
            $TESTDIR/language/should-not-work \
            $TESTDIR/cdm \
            $TESTDIR/cdm/ps \
            $TESTDIR/cdm/star
            $TESTDIR/language-behaviour/arrays \
            $TESTDIR/language-behaviour/broken \
	        $TESTDIR/language-behaviour/compounds \
            $TESTDIR/language-behaviour/control_structures \
            $TESTDIR/language-behaviour/datatypes \
	        $TESTDIR/language-behaviour/IO \
	        $TESTDIR/language-behaviour/logic \
	        $TESTDIR/language-behaviour/mappers \
	        $TESTDIR/language-behaviour/math \
	        $TESTDIR/language-behaviour/params \
            $TESTDIR/language-behaviour/procedures \
            $TESTDIR/language-behaviour/strings \
	        $TESTDIR/language-behaviour/variables \
	        $TESTDIR/language-behaviour/cleanup \
      	    $TESTDIR/bugs \
	        $TESTDIR/documentation/tutorial \
            $TESTDIR/functions \

            # Site testing test-group
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
            #$TESTDIR/sites/stampede \
            $TESTDIR/sites/swan  \

 	        # Remote-cluster IO tests
	        $TESTDIR/stress/IO/beagle \
            $TESTDIR/stress/IO/bagOnodes \
            $TESTDIR/stress/IO/multiple \
            $TESTDIR/stress/IO/uc3 \

            # Language stress tests
            $TESTDIR/stress/internals \

	        # Remote-cluster Apps tests - MODIS
            $TESTDIR/stress/apps/modis_beagle  \
            $TESTDIR/stress/apps/modis_local   \
	        $TESTDIR/stress/apps/modis_midway  \
	        $TESTDIR/stress/apps/modis_uc3     \
            # $TESTDIR/stress/apps/modis_multiple\

            # Local stress tests
            $TESTDIR/stress/internals \
            # Local cluster tests.
            $TESTDIR/stress/local_cluster \
            $TESTDIR/stress/random_fail \
            $TESTDIR/stress/jobs_per_node \

       	    # Recursive Test invocation
	        $TESTDIR/multi_remote

          )

checkvars WORK
