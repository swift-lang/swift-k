
# Used by vdl-int.k after workflow completion
# Flushes remaining GATHER output by using cdm_lib

# Must define some _swiftwrap functions so they can be called by cdm_lib

logstate() {
	log "Progress " `date +"%Y-%m-%d %H:%M:%S.%N%z"` ${*}
}

log() {
	echo ${*}
}

checkError() {
	if [ "$?" != "0" ]; then
		fail $@
	fi
}

fail() {
	EC=$1
	shift 
	log ${*}
	exit $EC
}

{ 
	set -x
	GATHER_DIR=$1
	GATHER_TARGET=$2
	ID=cleanup-$3

	if [ "X$GATHER_DIR" == "X" ]; then
		fail 254 "Not specified: GATHER_DIR"
	fi
	
	DIR=$( dirname $0 )
	source ${DIR}/cdm_lib.sh
	
	cdm_gather_cleanup
} >> /sandbox/cdm_cleanup.txt

# Local Variables: 
# mode: sh
# sh-basic-offset: 8
# End:
