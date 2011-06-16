
# GROUPLIST definition to run all local tests

GROUPLIST=( $TESTDIR/language-behaviour \
            # $TESTDIR/language/working \
            # $TESTDIR/local \
            # $TESTDIR/language/should-not-work \
            # $TESTDIR/cdm \
            # $TESTDIR/cdm/ps \
            # $TESTDIR/cdm/star
            $TESTDIR/language-behaviour/arrays \
	    $TESTDIR/language-behaviour/mappers \
	    $TESTDIR/language-behaviour/IO \
	    $TESTDIR/language-behaviour/iterators \
	    $TESTDIR/language-behaviour/logic \
	    $TESTDIR/language-behaviour/control_structures \
	    $TESTDIR/language-behaviour/procedures \
	    $TESTDIR/language-behaviour/arithmetic \
            $TESTDIR/language-behaviour/strings \
	    $TESTDIR/documentation/ \
	    # $TESTDIR/cdm/ps/pinned
	    # $TESTDIR/site/intrepid
          )

checkvars WORK
