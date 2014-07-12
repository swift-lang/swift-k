#!/bin/bash
shopt -s nullglob

printhelp() {
  echo "suite.sh <options> <output>"
  echo ""
  echo "usage:"
  printf "\t -a         Do not run ant dist                \n"
  printf "\t -c         Do not remove dist (clean)         \n"
  printf "\t -f         Generate plain text output file    \n"
  printf "\t -h         This message                       \n"
  printf "\t -k <N>     Skip first N tests                 \n"
  printf "\t -n <N>     Run N tests and quit               \n"
  printf "\t -p         Do not build the package           \n"
  printf "\t -s         Do not do a fresh svn checkout     \n"
  printf "\t -t         Tree mode (alias: -a,-c,-g,-p,-s)  \n"
  printf "\t -x         Do not continue after a failure    \n"
  printf "\t -v         Verbose (set -x, HTML comments)    \n"
  printf "\t -l         Stress level < 1/ 2/ 3/ 4>         \n"
  printf "\t -o output  Location for cog and output        \n"
  printf "\t -z file    Set environment for entire run     \n"
  printf "\t <GROUP>    GROUP argument                     \n"
}

# Defaults:
TEXTREPORT=0
DEFAULT_TIMEOUT=30 # seconds
RUN_ANT=1
STRESS=2
# If true, run "ant clean"
CLEAN=1
SKIP_TESTS=0
NUMBER_OF_TESTS=1000000 # Run all tests by default
BUILD_PACKAGE=1
SKIP_CHECKOUT=0
ALWAYS_EXITONFAILURE=0
VERBOSE=0
TOTAL_TIME=0
INDIVIDUAL_TEST_TIME=0
COLORIZE=0
# The directory in which to start:
TOPDIR=$( cd ../../../.. && echo $PWD )
CRTDIR=$PWD

# Disable usage stats in test suite
export SWIFT_USAGE_STATS=0

while [ $# -gt 0 ]; do
  case $1 in
    -a)
      RUN_ANT=0
      shift;;
    -c)
      CLEAN=0
      shift;;
    -f)
      TEXTREPORT=1
      shift;;
    -h|--h|-help|--help)
      printhelp
      exit 0;;
    -k)
      SKIP_TESTS=$2
      shift 2;;
    -n)
      NUMBER_OF_TESTS=$2
      shift 2;;
    -o)
      TOPDIR=$2
      shift 2;;
    -p)
      BUILD_PACKAGE=0
      shift;;
    -s)
      SKIP_CHECKOUT=1
      shift;;
    -t)
      # "Tree mode"
      RUN_ANT=0
      CLEAN=0
      BUILD_PACKAGE=0
      SKIP_CHECKOUT=1
      shift;;
    -x)
      ALWAYS_EXITONFAILURE=1
      shift;;
    -l)
      STRESS=$2
      shift 2;;
    -z)
      ENV_FILE=$2
      shift 2;;
    -v)
      VERBOSE=1
      shift;;
    -r)
      COLORIZE=1
      shift;;
    *)
      GROUPARG=$1
      shift;;
  esac
done

if (( VERBOSE )); then
  set -x
  HTML_COMMENTS=1
fi

if (( $COLORIZE )); then
	LGREEN="\033[1;32m"
	YELLOW="\033[1;33m"
	RED="\033[1;31m"
	GRAY="\033[0;37m"
else
	LGREEN=""
	YELLOW=""
	RED=""
	GRAY=""
fi

export STRESS="S$STRESS"
if [ -n "$ENV_FILE" ]
then
  source $ENV_FILE || echo "Could not load $ENV_FILE"
fi

# Iterations per test (may want to run each test multiple times?)
ITERS_LOCAL=1

LOGCOUNT=0
SEQ=1
DATE=$( date +"%Y-%m-%d" )
TIME=$( date +"%T" )
HOURMINSEC=$( date +"%H%M%S" )

RUNDIRBASE="run-$DATE"
RUNDIR=$TOPDIR/$RUNDIRBASE
LOGBASE=$RUNDIRBASE/tests.log
LOG=$TOPDIR/$LOGBASE
OUTPUT=$RUNDIR/exec.out

#Specifying the Path for the plain Text File.
REPORT_PATH=$RUNDIRBASE/tests-$DATE.txt
REPORT=$TOPDIR/$REPORT_PATH

#Specifying the path for the HTML output file
HTMLPATH=$RUNDIRBASE/tests-$DATE.html
HTML=$TOPDIR/$HTMLPATH

BRANCH=trunk
#BRANCH="branches/tests-01"

SCRIPTDIR=$( cd $( dirname $0 ) ; /bin/pwd )

TESTCOUNT=0
TESTSFAILED=0
TESTSPASSED=0
MASTER_PID=$$

# PIDs to kill if suite.sh is killed:
PROCESS_PID=
MONITOR_PID=

# When true, we should exit:
SHUTDOWN=0

echo "RUNNING_IN:  $RUNDIR"
echo "HTML_OUTPUT: $HTML"

TESTDIR=$TOPDIR/cog/modules/swift/tests

# Gensites will now check the variables as needed 
checkvars() {
 return
}

checkfail() {
  ERR=$?
  shift
  MSG=${*}
  if [[ $ERR != 0 ]]; then
    echo "FAILED($ERR): $MSG"
    exit $ERR
  fi
}

crash() {
  MSG=$1
  echo $MSG
  exit 1
}

verbose() {
  MSG="${*}"
  (( $VERBOSE )) && echo $MSG
  return 0
}

shutdown_trap() {
  SHUTDOWN=1
  printf "\nshutdown_trap: kill: process: $PROCESS_PID\n"
  kill $PROCESS_PID
}

header() {
	if [ $TEXTREPORT == 1 ]; then
		HEADER=$SCRIPTDIR/html/header.txt
  		HOST=$( hostname )
  		SEDCMD="s/_DATE_/$DATE/;s/_TIME_/$TIME/;s/_HOST_/$HOST"/
  		sed $SEDCMD < $HEADER > $REPORT
  		FIRSTTEST=1
	else
		CURRENT=$SCRIPTDIR/html/current.html
		sed "s@_HTMLBASE_@$HTMLPATH@" < $CURRENT > $TOPDIR/current.html
  		HEADER=$SCRIPTDIR/html/header.html
  		HOST=$( hostname )
  		SEDCMD="s/_DATE_/$DATE/;s/_TIME_/$TIME/;s/_HOST_/$HOST"/
  		sed $SEDCMD < $HEADER > $HTML
  		FIRSTTEST=1
	fi
}

html() {
  printf "$@\n" >>$HTML
  [ $? != 0 ] && echo "html(): write error!" && exit 1
}

html_h1() {
  TEXT=$1
  html "<h1>$TEXT</h1>"
}

html_b() {
  TEXT=$1
  html "<b>$TEXT</b>"
}

html_a_name() {
  NAME=$1
  html "<a name=\"$NAME\">"
}

html_a_href() {
  HREF=$1
  TEXT=$2
  html "<a href=\"$HREF\">$TEXT</a>"
}

html_table() {
  OPTS=""
  while [[ ${#*} > 0 ]]
  do
    case $1 in
      cellpadding)
        OPTS="${OPTS}cellpadding=\"$2\" "
        shift 2;;
      border)
        OPTS="${OPTS}border=\"$2\" "
        shift 2;;
      esac
  done
  html "<table $OPTS>"
}

html_~table() {
  html "</table>"
}

html_tr() {
  CLASS=$1
  if [ -n $CLASS ]; then
    html "<tr class=\"$CLASS\">"
  else
    html "<tr>"
  fi
}

html_~tr() {
  html "</tr>"
}

html_th() {
  COLSPAN=$1
  if [ -n $COLSPAN ]; then
    html "<th colspan=\"$COLSPAN\">"
  else
    html "<th>"
  fi
}

html_~th() {
  html "</th>"
}

html_td() {
  OPTS=""
  while [[ ${#*} > 0 ]]
  do
    case $1 in
      align)
        OPTS="${OPTS}align=\"$2\" "
        shift 2;;
      class)
        OPTS="${OPTS}class=\"$2\" "
        shift 2;;
      title)
        OPTS="${OPTS}title=\"$2\" "
        shift 2;;
      width)
        OPTS="${OPTS}width=\"$2\" "
        shift 2;;
    esac
  done
  html "<td $OPTS>"
}

html_~td() {
  html "</td>"
}

html_~body() {
  html "</body>"
}

html_~html() {
  html "</html>"
}

html_comment() {
  COMMENT="$1"
  (( HTML_COMMENTS == 1 )) && html "<!-- $COMMENT -->"
}

footer() {
  html_~tr
  html_~table
  html_~tr
  html_~table
  html_b "End of tests."
  html_~body
  html_~html
}

printlist() {
  while [[ $1 != "" ]]; do
    echo $1
    shift
  done
}

outecho() {
  TYPE=$1
  shift
  echo "<$TYPE>$1|$2|$3|$4|$5|$6|$7|$8|$9|"
}

#Create the table, and the links for the test results HTML output
start_test_results() {
  html_h1 "Test results"
  html_a_name "tests"
  html_a_href "tests.log" "Output log from tests"
  html_table border 0 cellpadding 1
}

# Create either HTML or plain text report.
# $TEXTREPORT monitor whether the report will be plain text or HTML
output_report() {

	TYPE=$1
	LABEL="$2"  # Text on link to output
	CMD=$3    # Command issued (td title)
	RESULT=$4 # Passed or Failed

	if [ $TEXTREPORT == 1 ]; then
		if [ "$TYPE" == "test" ]; then
			if [ "$RESULT" == "Passed" ]; then
				printf %-10.10s "success">>$REPORT
			else
				echo -e "${RED}FAILED${GRAY}"
				cat $RUNDIR/$OUTPUT < /dev/null
				printf %-10.10s "failure">>$REPORT
			fi

		elif [ "$TYPE" == "package" ]; then
			BINPACKAGE=$2
		else
			printf $@>>$REPORT
		fi
	else
		if [ "$TYPE" == "test" ]; then
			# WIDTH=$( width "$LABEL" )
			if [ "$RESULT" == "Passed" ]; then
				html_td class "success" width 25 title "$CMD"
				html_a_href "$TESTNAMEDIR/$OUTPUT" "$LABEL"
			elif [ "$RESULT" == "None" ]; then
				html_td width 25
				html "&nbsp;&nbsp;"
				html_~td
			else
				echo -e "${RED}FAILED${GRAY}"
				cat $RUNDIR/$OUTPUT < /dev/null
				html_td class "failure" width 25 title "$CMD"
				html_a_href "$TESTNAMEDIR/$OUTPUT" "$LABEL"
			fi
	    	html_~td
	  	elif [ "$TYPE" == "package" ]; then
	    	BINPACKAGE=$2
	  	else
		    html $@
	  	fi
	fi
}

start_group() {
  G=$1
  echo
  echo -e "${LGREEN}/----------------------------------------------------"
  echo "|"
  echo "| $G"
  echo "|"
  echo -e "\\----------------------------------------------------${GRAY}"
  echo
  if [ $TEXTREPORT == 1 ]; then
  	  stars
	  printf "$G\n">>$REPORT
	  printf %-5.5s "#">>$REPORT
	  printf %-60.60s "Filename">>$REPORT
	  printf %-10.10s "Test">>$REPORT
	  printf %-10.10s "Setup.sh" "Check.sh" "Clean.sh">>$REPORT
	  printf "\n">>$REPORT
  else
	  html_tr group
	  html_th 4
	  html "$G"
	  html_~th
	  html_~tr

	 html_tr group
	 html_td
	 html "#"
	 html_~td
	 html_td
	 html "Test name"
	 html_~td
	 html_td
	 html "Helper Scripts"
	 html_~td
	 html_td
	 html "Execution Time"
	 html_~td
	 html_~tr
  fi
}

start_row() {
	if [ $TEXTREPORT == 1 ]; then
		printf %-5.5s "$TESTCOUNT">>$REPORT
		printf %-60.60s "$TESTNAME">>$REPORT
	else
		html_tr testline
	  	html_td align right width 50
	    html "<b>$TESTCOUNT</b>"
	    html "&nbsp;"
	    html_~td
	    html_td align left
	    html "&nbsp;"
	    if [[ -n $TESTLINK ]]; then
	      html_a_href $TESTLINK $TESTNAME
	    else
	      html $TESTNAME
	    fi
	    html "&nbsp;"
	    html_~td
	    html_td
	    html_table
	    html_tr
	fi
    SEQ=1
}

end_row() {
	if [ $TEXTREPORT == 1 ]; then
		printf "\n">>$REPORT
	else
		html_~tr
		html_~table
		html_~td
   	        html_td align right
   	        html "$INDIVIDUAL_TEST_TIME seconds"
   	        html_~td
		html_~tr
	fi
}

stars() {
  for i in {1..120}
  do
    printf "*">>$REPORT
  done
  printf "\n">>$REPORT
  echo
}

banner() {
  MSG=$1
  FILE=$2

  if [ "$FILE" == "" ]; then
    BANNER_OUTPUT=$LOG
  else
    BANNER_OUTPUT=$FILE
  fi
  {
    echo ""
          # stars
    echo "* $MSG"
	  # stars
  } >> $BANNER_OUTPUT
}

# Check for early bailout condition
check_bailout() {
  if [ "$EXITONFAILURE" == "true" ]; then
    if [ "$EXITCODE" != "0" ]; then
      exit $EXITCODE
    fi
  fi
}

# Translate (global) test exit code into result (Passed/Failed) string
result() {
  if [ "$EXITCODE" == "0" ]; then
    echo "Passed"
  else
    echo "Failed"
  fi
}

# Note that killing Swift/Coasters may result in a delay as
# Coasters shuts down: cf. CoasterService.addLocalHook()
process_trap() {
  PROCESS_INTERNAL_PID=$1
  echo "process_trap: killing: $PROCESS_INTERNAL_PID"
  ps -o "pid,ppid"|sed 1d | while read PROC
  do
     PROC_PID=$( echo $PROC | awk '{print $1}' )
     PROC_PPID=$( echo $PROC | awk '{print $2}' )
     if [ $PROC_PPID == $PROCESS_INTERNAL_PID ]
     then
        kill $PROC_PID
     fi
  done
}

# Execute process in the background
process_exec() {
  PROG=$( basename $1 )
  if [[ $PROG == "swift" ]]; then
    # Get SwiftScript name
    PROG=$( echo $@ | sed 's/.*\( [^ ]*.swift\)/\1/' )
    PROG=$( basename $PROG )
  fi

  echo -e "Executing: $PROG"
  echo -e "\nExecuting: $@\n" >> $LOG

  rm -f $OUTPUT

  "$@" > $OUTPUT 2>&1 &
  PROCESS_INTERNAL_PID=$!
  trap "process_trap $PROCESS_INTERNAL_PID" SIGTERM
  wait $PROCESS_INTERNAL_PID
  EXITCODE=$?

  if [ "$EXITCODE" == "127" ]; then
    echo "Command not found: $@" > $OUTPUT
  fi
  if [ $EXITCODE != 0 ]; then
    cat $OUTPUT
  fi
  if [ -f $OUTPUT ]; then
    cat $OUTPUT >> $LOG
  fi
  (( $TEST_SHOULD_FAIL )) && EXITCODE=$(( ! $EXITCODE ))
  return $EXITCODE
}

# Execute as part of test case
# Equivalent to monitored_exec() (but w/o monitoring)
test_exec() {

  printf "\nExecuting: $@\n" >>$LOG

  rm -f $OUTPUT
  "$@" > $OUTPUT 2>&1
  EXITCODE=$?

  if [ "$EXITCODE" == "127" ]; then
    echo "Command not found: $@" > $OUTPUT
  fi

  if [ -f $OUTPUT ]; then
    cat $OUTPUT >> $LOG
  fi

  RESULT=$( result )
  output_report test $SEQ "$LASTCMD" $RESULT $OUTPUT

  check_bailout

  let "SEQ=$SEQ+1"
  return $EXITCODE
}

# Background process monitoring function
# To be executed in the background as well
# If the monitored process times out, monitor() kills it and writes
# a message to OUTPUT
# If monitor() kills the process, it returns 0
# Otherwise, return non-zero (as would result from killing
# this function)

# sleep can be hard to kill:
SLEEP_PID=
monitor() {
  (( $VERBOSE )) && set -x

  PID=$1
  TIMEOUT=$2 # seconds
  OUTPUT=$3

  V=$TESTCOUNT

  sleep $TIMEOUT &
  SLEEP_PID=$!
  trap trap_sleep SIGINT SIGQUIT SIGTERM
  wait
  [ $? != 0 ] && verbose "monitor($V) cancelled" && return 0

  if ps | grep $PID > /dev/null 2>&1
  then
    echo "monitor: killing test process $PID"
    touch killed_test
    kill $PID
    KILLCODE=$?
    if [ $KILLCODE == 0 ]; then
      echo "monitor: killed test process"
    fi
  fi

  sleep 1
  MSG="suite.sh: monitor: killed: exceeded $TIMEOUT seconds"
  echo "$MSG" >> $OUTPUT

  return 1
}

trap_sleep() {
  verbose "killing sleep: $SLEEP_PID"
  kill $SLEEP_PID
}

# Execute given command line in background with monitor
# Otherwise equivalent to test_exec()
# usage: monitored_exec <TIMEOUT> <command> <args>*
monitored_exec()
{
  TIMEOUT=$1
  shift

  START=$( date +%s )

  process_exec "$@" &
  PROCESS_PID=$!

  monitor $PROCESS_PID $TIMEOUT $OUTPUT &
  MONITOR_PID=$!

  wait $PROCESS_PID
  EXITCODE=$?

  STOP=$( date +%s )

  # If the test was killed, monitor() may have work to do
  rm killed_test > /dev/null 2>&1 && sleep 5
  if ps -p $MONITOR_PID > /dev/null 2>&1
  then
     verbose "killing monitor: $MONITOR_PID..."
     kill $MONITOR_PID
  fi

  INDIVIDUAL_TEST_TIME=$(( STOP-START ))
  TOTAL_TIME=$(( INDIVIDUAL_TEST_TIME+TOTAL_TIME ))
  echo -e "${YELLOW}TOOK (seconds): $INDIVIDUAL_TEST_TIME${GRAY}"
  RESULT=$( result )

#Verifies the value of $RESULT, if the test was successful
#increases $TESTSPASSED by 1, if the test Failed
#increases $TESTSFAILED by 1.
  if [ "$RESULT" != "Passed" ]; then
  	let TESTSFAILED=$TESTSFAILED+1
  else
  	let TESTSPASSED=$TESTSPASSED+1
  fi


  LASTCMD="$@"
  output_report test $SEQ "$LASTCMD" $RESULT $OUTPUT

  check_bailout

  let "SEQ=$SEQ+1"
}

# Execute helper script (setup, check, or clean)
script_exec() {
  SCRIPT=$1
  SYMBOL="$2"

  process_exec $SCRIPT
  RESULT=$( result )

  output_report test "$SYMBOL" "$LASTCMD" $RESULT

  check_bailout
}

stage_files() {
	GROUP=$1
	NAME=$2

	RESULT="None"
	if [ -f "$GROUP/$NAME.in" ]; then
		echo "Copying input: $NAME.in"
		cp -v $GROUP/$NAME.in . 2>&1 >> $OUTPUT
		if [ "$?" != 0 ]; then
			RESULT="Failed"
		fi
		if [ "$RESULT" == "None" ]; then
			RESULT="Passed"
		fi
	fi
	for INPUT in $GROUP/$NAME.*.in; do
		IN=`basename $INPUT`
		echo "Copying input: $IN"
		cp -v $INPUT . 2>&1 >> $OUTPUT
		if [ "$?" != 0 ]; then
			RESULT="Failed"
		fi
		if [ "$RESULT" == "None" ]; then
			RESULT="Passed"
		fi
	done

	output_report test "s" "setup" $RESULT

	check_bailout
}

check_outputs() {
	GROUP=$1
	NAME=$2

	RESULT="None"

	for EXPECTED in $GROUP/$NAME.*.expected; do
		BNE=`basename $EXPECTED .expected`
		echo -n "Checking output: $BNE "
		diff $BNE $EXPECTED 2>&1 >> $OUTPUT
		if [ "$?" != "0" ]; then
			RESULT="Failed"
                        echo Failed
                else
                        echo OK
                fi
		if [ "$RESULT" == "None" ]; then
			RESULT="Passed"
		fi
	done

	if [ "$RESULT" == "None" ]; then
		html_td width 25
   		html "&nbsp;&nbsp;"
   		html_~td
	fi

	output_report test "&#8730;" "check" $RESULT

	check_bailout
}

# Execute Swift test case w/ setup, check, clean
swift_test_case() {
  SWIFTSCRIPT=$1
  NAME=${SWIFTSCRIPT%.swift}

  if grep -q "SKIP-THIS-TEST" $SWIFTSCRIPT ; then
    echo SKIP-THIS-TEST
    INDIVIDUAL_TEST_TIME=0
    return 0
  fi

  SOURCESCRIPT=$NAME.source.sh
  SETUPSCRIPT=$NAME.setup.sh
  CHECKSCRIPT=$NAME.check.sh
  CLEANSCRIPT=$NAME.clean.sh
  TIMEOUTFILE=$NAME.timeout
  ARGSFILE=$NAME.args

  TEST_SHOULD_FAIL=0
  OUTPUT=$NAME.setup.stdout

  if [ -x "$GROUP/$SOURCESCRIPT" ]; then
    cp "$GROUP/$SOURCESCRIPT" .
    source ./$SOURCESCRIPT 
  fi

  if [ -x "$GROUP/$SETUPSCRIPT" ]; then
    cp "$GROUP/$SETUPSCRIPT" .
    script_exec ./$SETUPSCRIPT "S"
  else
    stage_files $GROUP $NAME
  fi

  ARGS=""
  if [ -f $GROUP/$ARGSFILE ]; then
    cp "$GROUP/$ARGSFILE" .
    ARGS=`cat $GROUP/$ARGSFILE`
  elif [ -f $ARGSFILE ]; then
    ARGS=`cat $ARGSFILE`
  fi

  CDM=
  [ -r fs.data ] && CDM="-cdm.file fs.data"

  (( TESTCOUNT++ ))

  TIMEOUT=$( gettimeout $GROUP/$TIMEOUTFILE )
  if [ -f "$GROUP/$TIMEOUTFILE" ]; then
     cp "$GROUP/$TIMEOUTFILE" .
  fi
  grep THIS-SCRIPT-SHOULD-FAIL $GROUP/$SWIFTSCRIPT > /dev/null
  TEST_SHOULD_FAIL=$(( ! $?  ))

  OUTPUT=$NAME.stdout
  monitored_exec $TIMEOUT swift $CDM $SWIFTSCRIPT $ARGS

  TEST_SHOULD_FAIL=0
  if [ -x "$GROUP/$CHECKSCRIPT" ]; then
     cp "$GROUP/$CHECKSCRIPT" .
     OUTPUT=$NAME.check.stdout  
     script_exec ./$CHECKSCRIPT "&#8730;"
  else
    check_outputs $GROUP $NAME
  fi

   OUTPUT=$NAME.clean.stdout
  if [ -x "$GROUP/$CLEANSCRIPT" ]; then
    cp "$GROUP/$CLEANSCRIPT" .
    script_exec ./$CLEANSCRIPT "C"
  else
   html_td width 25
   html "&nbsp;&nbsp;"
   html_~td
  fi
}
# Execute shell test case w/ setup, check, clean
script_test_case() {
  SHELLSCRIPT=$1
  NAME=${SWIFTSCRIPT%.swift}

  stage_files $GROUP $NAME

  SETUPSCRIPT=$NAME.setup.sh
  CHECKSCRIPT=$NAME.check.sh
  CLEANSCRIPT=$NAME.clean.sh
  TIMEOUTFILE=$NAME.timeout

  TEST_SHOULD_FAIL=0
  OUTPUT=$NAME.clean.stdout
  if [ -x "$GROUP/$SETUPSCRIPT" ]; then
    cp "$GROUP/$SETUPSCRIPT" .
    script_exec ./$SETUPSCRIPT "S"
  else
   html_td width 25
   html "&nbsp;&nbsp;"
   html_~td
  fi

  (( TESTCOUNT++ ))

  # Not using background for script tests yet
  # TIMEOUT=$( gettimeout $GROUP/$TIMEOUTFILE )

  if [ -x $GROUP/$SETUPSCRIPT ]; then
    script_exec $GROUP/$SETUPSCRIPT "S"
  else
   html_td width 25
   html "&nbsp;&nbsp;"
   html_~td
  fi

  OUTPUT=$NAME.stdout

  if [ -x $GROUP/$SHELLSCRIPT ]; then
    script_exec $SHELLSCRIPT "X"
  else
   html_td width 25
   html "&nbsp;&nbsp;"
   html_~td
  fi

  OUTPUT=$NAME.check.stdout
  if [ -x "$GROUP/$CHECKSCRIPT" ]; then
    cp "$GROUP/$CHECKSCRIPT" .
    script_exec ./$CHECKSCRIPT "&#8730;"
  else
   html_td width 25
   html "&nbsp;&nbsp;"
   html_~td
  fi

  OUTPUT=$NAME.clean.stdout
  if [ -x "$GROUP/$CLEANSCRIPT" ]; then
    cp "$GROUP/$CLEANSCRIPT" .
    script_exec ./$CLEANSCRIPT "C"
  else
   html_td width 25
   html "&nbsp;&nbsp;"
   html_~td
  fi
}

# All timeouts in this script are in seconds
gettimeout() {
  FILE=$1

  if [ -f $FILE ]; then
    cat $FILE
  else
    echo $DEFAULT_TIMEOUT
  fi
  return 0
}

ssexec() {
  SEQSAVE=$SEQ
  SEQ=$1
  shift
  banner "$TEST (group $SEQ)"
  echo "Executing $TEST (group $SEQ)"
  aexec "$@"
  ptest
  SEQ=$SEQSAVE
}

# Fake exec
fexec() {
  banner "$TEST (faked)"
  echo "Faking $TEST"
  EXITCODE=0
  LASTCMD=""
  # ptest
}

build_package() {
  TEST="Package"
  test_exec cd $SWIFT_HOME/lib
  test_exec rm -f castor*.jar *gt2ft*.jar ant.jar
  test_exec cd $TOPDIR
  test_exec tar -pczf $RUNDIR/swift-$DATE.tar.gz $SWIFT_HOME
  output_report package "swift-$DATE.tar.gz"
}

# Generate the CDM file, fs.data
group_fs_data() {
  if [ -f $GROUP/fs.template.data ]; then
    sed "s@_PWD_@$PWD@" < $GROUP/fs.template.data > fs.data
    [ $? != 0 ] && crash "Could not create fs.data!"
    echo "Using: $GROUP/fs.template.data"
  else
    rm -f fs.data
  fi
}

# Generate swift.properties
group_swift_properties() {
  if [ -f $GROUP/swift.properties ]; then
    cp -v $GROUP/swift.properties .
    [ $? != 0 ] && crash "Could not copy swift.properties!"
  fi
}

# Obtain the group title
group_title() {
  if [ -r $GROUP/title.txt ]; then
    cat $GROUP/title.txt
  else
  	G=$GROUP
	PIECES=""
	while [ "$G" != "$CRTDIR" ]; do
		PIECE=`basename $G`
		PIECES="$PIECE/$PIECES"
		G=`dirname $G`
	done
	echo $PIECES
  fi
}

# Print the number of tests run, failed and passed.
# Revision:001
group_statistics(){
	if [ $TEXTREPORT == 1 ]; then
		printf "\n $TESTCOUNT Tests run\t$TESTSFAILED Tests failed\t$TESTSPASSED Tests succeeded.\tTotal time: $TOTAL_TIME seconds \n\n">>$REPORT
	else
	 	 html_tr class "group"
		 html_td class "neutral"
	 	 html "$TESTCOUNT Tests run"
		 html_~td
		 html_td class "success"
		 html "$TESTSPASSED Tests succeeded."
		 html_~td
		 if [ "$TESTSFAILED" == "0" ]; then
		 	html_td class "success"
		 else
		 	html_td class "failure"
		 fi
		 html "$TESTSFAILED Tests failed."
		 html_~td
		 html_td class "neutral" align left
		 html "Total Time: $TOTAL_TIME seconds"
		 html_~td
		 html_~tr
	fi
}

# Execute all tests in current GROUP
test_group() {


  SWIFTS=$( echo $GROUP/*.swift )
  checkfail "Could not list: $GROUP"

  for TEST in $SWIFTS; do

    (( SKIP_COUNTER++ < SKIP_TESTS )) && continue
   # Use scriptname.repeat to determine number of test iterations
    SCRIPT_BASENAME=`basename $TEST .swift`
    TESTLINK="$TESTNAMEDIR/$TESTNAME"
    GROUP_DIRNAME=$( dirname $GROUP )
    if [ -f "$GROUP/$SCRIPT_BASENAME.repeat" ]; then
       ITERS_LOCAL=$( cat $GROUP/$SCRIPT_BASENAME.repeat )
    else
       ITERS_LOCAL=1
    fi 

    for (( i=0; $i<$ITERS_LOCAL; i=$i+1 )); do
      HOURMINSEC=$( date +"%H%M%S" )
      TESTNAME=$( basename $TEST )
      TESTNAMEDIR=`basename $TESTNAME .swift`-$HOURMINSEC
      TESTLINK="$TESTNAMEDIR/$TESTNAME"

      echo
      echo
      echo    "+--------------------------------------------------------------"
      echo -e "|   Test case: $LGREEN$TESTNAME$GRAY"
      echo    "+--------------------------------------------------------------"
      echo

      mkdir -p $TESTNAMEDIR
      pushd $TESTNAMEDIR > /dev/null 2>&1
      cp $TEST .    
      group_swift_properties
      group_fs_data      
      start_row
      swift_test_case $TESTNAME
      (( $TESTCOUNT >= $NUMBER_OF_TESTS )) && return
      (( $SHUTDOWN )) && return
      end_row
      popd > /dev/null 2>&1
    done
  done
    group_statistics
    TOTAL_TIME=0
    TESTCOUNT=0
    TESTSPASSED=0
    TESTSFAILED=0


  SCRIPTS=$( echo $GROUP/*.test.sh )
  checkfail "Could not list: $GROUP"
  for TEST in $SCRIPTS; do

    (( SKIP_COUNTER++ < SKIP_TESTS )) && continue
    HOURMINSEC=$( date +"%H%M%S" )
    TESTNAME=$( basename $TEST )
    TESTNAMEDIR=`basename $TESTNAME .swift`-$HOURMINSEC
    mkdir -p $TESTNAMEDIR
    pushd $TESTNAMEDIR > /dev/null 2>&1
    cp -v $GROUP/$TESTNAME .
    start_row
    for ((i=0; $i<$ITERS_LOCAL; i=$i+1)); do
      script_test_case $TESTNAME
      (( $TESTCOUNT >= $NUMBER_OF_TESTS )) && return

      (( $SHUTDOWN )) && return
    done
    end_row
  done

}

#checkvars GROUPARG
echo "GROUP ARGUMENT: $GROUPARG"
if [[ $GROUPARG != /* ]]; then
  # Adjust relative path
  GROUPARG=$PWD/$GROUPARG
fi
if [[ -f $GROUPARG ]]; then
  GROUPLISTFILE=$GROUPARG
  source $GROUPLISTFILE || exit 1
elif [[ -d $GROUPARG ]]; then
  GROUPLIST=( $GROUPARG )
else
  echo "Unusable GROUP argument: $GROUPARG"
  exit 1
fi

cd $TOPDIR
[ $? != 0 ] && crash "Could not use given TOPDIR: $TOPDIR"

mkdir -p $RUNDIR
[ $? != 0 ] && crash "Could not mkdir: $RUNDIR"

date > $LOG

# Here the report starts.
# Call to function header()
header
if [ $TEXTREPORT == 1 ]; then
	printf "Test Results\n\n">>$REPORT
else
	start_test_results
fi
cd $TOPDIR
start_group "Build"
TESTLINK=
EXITONFAILURE=true
OUTPUT=checkout.stdout
if [ "$SKIP_CHECKOUT" != "1" ]; then
  TESTNAME="Checkout CoG"
  start_row
  COG="https://cogkit.svn.sourceforge.net/svnroot/cogkit/$COG_VERSION/src/cog"
  test_exec svn co $COG
  end_row

  TESTNAME="Checkout Swift"
  start_row
  test_exec cd cog/modules
  test_exec svn co https://svn.ci.uchicago.edu/svn/vdl2/$SWIFT_VERSION swift
  end_row
fi

TESTNAME="Compile"
start_row

OUTPUT=compile.stdout
# Exit early if the Swift directory is not there
if [[ ! -d $TOPDIR/cog/modules/swift ]]
then
  echo "Could not find swift source directory"
  echo "TOPDIR: $TOPDIR"
  echo "Looked for $TOPDIR/cog/modules/swift"
  crash
fi

test_exec cd $TOPDIR/cog/modules/swift
if (( $CLEAN )); then
  test_exec rm -rf dist
fi
if (( $RUN_ANT )); then
  test_exec ant -quiet dist
fi
SWIFT_HOME=$TOPDIR/cog/modules/swift/dist/swift-svn
OUTPUT=compile.stdout
OUTPUT=build.stdout
if [ $BUILD_PACKAGE = "1" ]; then
  build_package
fi

end_row

PATH=$SWIFT_HOME/bin:$PATH
cd $TOPDIR
echo "USING: $( which swift )"
cd $RUNDIR

if [ $ALWAYS_EXITONFAILURE != "1" ]; then
  EXITONFAILURE=false
fi

trap "shutdown_trap" SIGINT SIGTERM

SKIP_COUNTER=0

GROUPCOUNT=1
for G in ${GROUPLIST[@]}; do
  export GROUP=$G
  echo "GROUP: $GROUP"
  [ -d $GROUP ] || crash "Could not find GROUP: $GROUP"
  TITLE=$( group_title )

  start_group "Group $GROUPCOUNT: $TITLE"
  test_group
  (( GROUPCOUNT++ ))
  (( $TESTCOUNT >= $NUMBER_OF_TESTS )) && end_row | html_~tr | group_statistics && break
  (( $SHUTDOWN )) && break
done

footer
exit 0

# Local Variables:
# sh-basic-offset: 2
# End:
