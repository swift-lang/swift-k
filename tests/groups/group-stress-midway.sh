# GROUPLIST definition to run all local tests                            \

GROUPLIST=(
             $TESTDIR/stress/apps/modis_local \
             $TESTDIR/stress/apps/modis_midway \
             $TESTDIR/stress/apps/modis_beagle \
             $TESTDIR/stress/apps/modis_osgc \
             $TESTDIR/stress/apps/modis_multiple \
          )

checkvars WORK
