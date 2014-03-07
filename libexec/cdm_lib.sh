
# Source this for CDM shell functions

log "Reading cdm_lib.sh ..."

# Do a CDM lookup
cdm_lookup() {
	CDM_PL=$1
	CDM_FILE=$2
	FILE=$3

	RESULT="DEFAULT"
	if [ ! -z $CDM_FILE ] && [ -f $CDM_PL ] && [ -f $CDM_FILE ]; then
		RESULT=$( perl $CDM_PL lookup $FILE < $CDM_FILE 2> $INFO )
		checkError 254 "cdm_lookup(): failed! (malformed CDM file?)"
	fi
	echo $RESULT
}

cdm_action() {
	log "CDM_ACTION: $@"

	local JOBDIR=$1  # Given jobdir
	local MODE=$2    # INPUT or OUTPUT
	local FILE=$3    # User file
	local POLICY=$4  # DIRECT, BROADCAST, ...
	shift 4
	local ARGS=$@
	
	local ACTUAL_FILE=$(echo $FILE | sed -e 's:__root__/:/:' -e 's:__parent__:..:')

	log "POLICY=$POLICY"
	case $POLICY in
		DIRECT)
			DIRECT_DIR=${ARGS[0]}
			[[ $DIRECT_DIR == "/" ]] && DIRECT_DIR=""
			log "CDM[DIRECT]: Linking to $DIRECT_DIR/$ACTUAL_FILE via $JOBDIR/$FILE"
			if [ $MODE == "INPUT" ]; then
				[ -f "$DIRECT_DIR/$ACTUAL_FILE" ]
				checkError 254 "CDM[DIRECT]: $DIRECT_DIR/$ACTUAL_FILE does not exist!"
				ln -s $DIRECT_DIR/$ACTUAL_FILE $JOBDIR/$FILE
				checkError 254 "CDM[DIRECT]: Linking to $DIRECT_DIR/$ACTUAL_FILE failed!"
			elif [ $MODE == "OUTPUT" ]; then
				mkdir -p $( dirname $DIRECT_DIR/$ACTUAL_FILE )
				checkError 254 "CDM[DIRECT]: mkdir -p $( dirname $DIRECT_DIR/$ACTUAL_FILE ) failed!"
				touch $DIRECT_DIR/$ACTUAL_FILE
				checkError 254 "CDM[DIRECT]: Touching $DIRECT_DIR/$ACTUAL_FILE failed!"
				ln -s $DIRECT_DIR/$ACTUAL_FILE $JOBDIR/$FILE
				checkError 254 "CDM[DIRECT]: Linking to $DIRECT_DIR/$ACTUAL_FILE failed!"
			else
				fail 254 "Unknown MODE: $MODE"
			fi
			;;
 		LOCAL)
			# TODO: Can/should we use this as a cache?
			TOOL=$1
			REMOTE_DIR=$2
			FLAGS=$3
			log "CDM[LOCAL]: TOOL=$TOOL FLAGS=$FLAGS REMOTE_DIR=$REMOTE_DIR ARGS=$ARGS"
 			log "CDM[LOCAL]: Copying $REMOTE_DIR/$ACTUAL_FILE to $JOBDIR/$FILE"
 			if [ $MODE == "INPUT" ]; then
 				[ -f "$REMOTE_DIR/$ACTUAL_FILE" ]
 				checkError 254 "CDM[LOCAL]: $REMOTE_DIR/$ACTUAL_FILE does not exist!"
				if [ $TOOL == "cp" ]; then
 					$TOOL $FLAGS $REMOTE_DIR/$ACTUAL_FILE $JOBDIR/$FILE
					checkError 254 "CDM[LOCAL]: cp failed!"
				elif [ $TOOL == "dd" ]; then
					$TOOL $FLAGS if=$REMOTE_DIR/$ACTUAL_FILE of=$JOBDIR/$FILE
					checkError 254 "CDM[LOCAL]: dd failed!"
				else
					fail 254 "CDM[LOCAL]: Unknown TOOL: $TOOL"
				fi
 			elif [ $MODE == "OUTPUT" ]; then
 				log "CDM[LOCAL]..." # This should probably be an error
 			else
 				fail 254 "Unknown MODE: $MODE"
 			fi
 			;;
		BROADCAST)
			BROADCAST_DIR=${ARGS[0]}
			if [ $MODE == "INPUT" ]; then
				log "CDM[BROADCAST]: Linking $JOBDIR/$FILE to $BROADCAST_DIR/$ACTUAL_FILE"
				[ -f "$BROADCAST_DIR/$ACTUAL_FILE" ]
				checkError 254 "CDM[BROADCAST]: $BROADCAST_DIR/$ACTUAL_FILE does not exist!"
				ln -s $BROADCAST_DIR/$FILE $JOBDIR/$ACTUAL_FILE
				checkError 254 "CDM[BROADCAST]: Linking to $BROADCAST_DIR/$ACTUAL_FILE failed!"
			else
				echo "CDM[BROADCAST]: Skipping output file: ${ACTUAL_FILE}"
			fi
			;;
		GATHER)
			if [ $MODE == "INPUT" ]; then
				fail 254 "Cannot GATHER an input file!"
			fi
    esac
}

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
# sh-basic-offset: 4
# tab-width: 4
# indent-tabs-mode: 1
# End:
