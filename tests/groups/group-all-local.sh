
# GROUPLIST definition to run all local tests

GROUPLIST=( $TESTDIR/language/working \
            $TESTDIR/local \
            $TESTDIR/language/should-not-work \
            # $TESTDIR/cdm \
            # $TESTDIR/cdm/ps \
            # $TESTDIR/cdm/star
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
	    $TESTDIR/bugs \
	    $TESTDIR/documentation/tutorial \
	    # $TESTDIR/cdm/ps/pinned
	    # $TESTDIR/site/intrepid
          )

checkvars WORK
