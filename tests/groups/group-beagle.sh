
# GROUPLIST definition to run on Beagle 

GROUPLIST=( $TESTDIR/local \
            $TESTDIR/providers/beagle/coasters \
            $TESTDIR/provdiers/beagle/pbs \
          )

checkvars WORK QUEUE PROJECT
