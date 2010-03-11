
# Source this for CDM shell functions

# Setup GATHER_DIR and some variables
cdm_gather_setup() {
	GATHER_DIR=$1

	logstate "GATHER_DIR $GATHER_DIR"
	mkdir -p $GATHER_DIR
	checkError 254 "Could not create: $GATHER_DIR"
	
	GATHER_LOCKFILE=$GATHER_DIR/.cdm.lock
	GATHER_MY_FILE=$GATHER_DIR/.cdm-$ID.lock
	GATHER_MY_OUTBOX=$GATHER_DIR/.cdm-outgoing-$ID
}

# Acquire the GATHER_LOCKFILE
cdm_gather_lock_acquire() {
	TRYING=1
	COUNT=0

	touch $GATHER_MY_FILE
	checkError 254 "Could not touch my file: $GATHER_MY_FILE"
	logstate "LOCK_ACQUIRE $GATHER_MY_FILE"
	while (( TRYING )); do
		ln $GATHER_MY_FILE $GATHER_LOCKFILE
		CODE=$?
		TRYING=$CODE
		if (( TRYING )); then
			logstate "LOCK_DELAY $GATHER_MY_FILE : $CODE"
			if (( COUNT++ > 10 )); then
				fail 254 "Could not acquire lock!"
				exit 1
			fi
			sleep $(( RANDOM % 10 ))
		fi
	done
}

# Move files from JOBDIR to GATHER_DIR
cdm_gather_import() {
	pushd jobs/$JOBDIR/$ID
	logstate "GATHER_IMPORT $GATHER_OUTPUT"
	mv $GATHER_OUTPUT $GATHER_DIR
	checkError 254 "Could not move output to $GATHER_DIR"
	popd
}

# Move files from GATHER_DIR to OUTBOX
# Note that this function modifies $IFS
cdm_gather_export() {
	RESULT=1
	pushd ${GATHER_DIR}
	IFS="
"
	FILES=( $( ls -A -I ".cdm*.lock" -I ".cdm-outgoing-*" ) )
	log "FILES: ${#FILES[@]}"
	mkdir -p $GATHER_MY_OUTBOX
	checkError 254 "Could not mkdir ${GATHER_MY_OUTBOX}"
	if (( ${#FILES} > 0 )); then
		mv ${FILES[@]} $GATHER_MY_OUTBOX
		checkError 254 "Could not move ${FILES[@]} to $GATHER_MY_OUTBOX"
		RESULT=0
	fi
	popd
	return $RESULT
}

# Release the GATHER_LOCKFILE
cdm_gather_lock_release() {
	logstate "LOCK_RELEASE $GATHER_LOCKFILE"
	unlink $GATHER_LOCKFILE
}

# Move files from (LFS) OUTBOX to (GFS) GATHER_TARGET 
cdm_gather_flush() {
	pushd ${GATHER_MY_OUTBOX}
	logstate "GATHER_TARGET $GATHER_TARGET"
	mkdir -p ${GATHER_TARGET}
	logstate "GATHER_FLUSH_START ${GATHER_TARGET}/cdm-gather-$ID.tar"
	TARBALL=${GATHER_TARGET}/cdm-gather-$ID.tar
	tar cf ${TARBALL} *
	checkError 254 "CDM[GATHER]: error writing: ${TARBALL}"
	logstate "GATHER_FLUSH_DONE"
	popd
}

# Called by _swiftwrap at the end of each job
cdm_gather_action() {
	GATHER_OUTPUT=${*}

	GATHER_DIR=$(    perl shared/cdm.pl property GATHER_DIR    < $CDM_FILE )
	GATHER_MAX=$(    perl shared/cdm.pl property GATHER_LIMIT  < $CDM_FILE )
	GATHER_TARGET=$( perl shared/cdm.pl property GATHER_TARGET < $CDM_FILE )

	cdm_gather_setup $GATHER_DIR 
	cdm_gather_lock_acquire 

	cdm_gather_import 

	USAGE=$( du -s --exclude=".cdm*" -B 1 $GATHER_DIR )
	USAGE=${USAGE%	*} # Chop off filename in output

	logstate "USAGE_CHECK $USAGE / $GATHER_MAX"

	FLUSH="no"
	if (( USAGE > GATHER_MAX )); then 
		FLUSH="yes"
		cdm_gather_export
	fi
	
	cdm_gather_lock_release
	if [ $FLUSH == "yes" ]; then 
		cdm_gather_flush 
	fi
}

# Called by cdm_cleanup.sh at the end of the workflow
cdm_gather_cleanup() {
	declare -p PWD DIR GATHER_DIR GATHER_TARGET ID
	cdm_gather_setup $GATHER_DIR 
	cdm_gather_lock_acquire 
	cdm_gather_export
	EXPORT_RESULT=$?
	cdm_gather_lock_release
	if (( EXPORT_RESULT == 0)); then
		cdm_gather_flush
	fi
}

# Local Variables: 
# mode: sh
# sh-basic-offset: 8
# End:
