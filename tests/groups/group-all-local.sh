
# GROUPLIST definition to run all local tests

GROUPLIST=( $TESTDIR/language-behaviour \
            $TESTDIR/language/working \
            $TESTDIR/local \
            $TESTDIR/language/should-not-work \
            $TESTDIR/cdm \
            $TESTDIR/cdm/ps \
            $TESTDIR/cdm/star
            $TESTDIR/cdm/ps/pinned
	    # $TESTDIR/site/intrepid
          )

checkvars WORK
