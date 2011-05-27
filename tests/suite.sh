#!/bin/bash

# USAGE NOTES:

# The script will (optionally) checkout Swift, run several tests in a
# subdirectory called run-DATE, and generate useful HTML output and
# tests.log .  Tests are grouped into test GROUPs.

# Usage: suite.sh <options>* <GROUPLISTFILE|GROUP>

# PRIMARY USAGE MODE
# Assuming your code is in /tmp/cog, where you
# have the conventional cog/modules/swift configuration,
# and you have done an ant dist, you can run
# suite.sh -t -o /tmp $PWD/tests/groups/group-all-local.sh
# or cd into /tmp and run
# suite.sh -t cog/modules/swift/tests/groups/group-all-local.sh
# The -t option is "Tree mode"- as in, "test my existing source tree"

# Run suite.sh -h for quick help
# When something goes wrong, find and check tests.log or use -v

# SWIFT LOCATION
# The TOPDIR (PWD by default) is set with the -o option.
# Code is checked out into this directory or must already exist there.
# The variables COG_VERSION and SWIFT_VERSION must be set for code checkout
# e.g. COG_VERSION=branches/4.1.8, SWIFT_VERSION=branches/release-0.92
# Swift is compiled and installed in its source tree
# The run is executed in RUNDIR (TOPDIR/RUNDIRBASE)
# The build test is started in TOPDIR
# Everything for a Swift test is written in its RUNDIR
# The temporary output always goes to OUTPUT (TOPDIR/exec.out)

# HELPER SCRIPTS
# Each *.swift test may be accompanied by a
# *.setup.sh, *.check.sh, and/or *.clean.sh script
# and a *.timeout specifier
# The scripts may setup and inspect files in RUNDIR including exec.out,
# which must be accessed in stdout.txt, because the currently running
# tested process writes to exec.out, stdout.txt is a copy.
# The GROUP scripts can read the GROUP variable
# The timeout number in the *.timeout file overrides the default
# timeout

# TEST STRUCTURE
# Tests are GROUPed into directories
# Each GROUP directory has:
#      1) a list of *.swift tests (plus *.sh scripts)
#      2) optionally a sites.template.xml
#      3) optionally a tc.template.data
#      4) optionally a fs.template.data
#      5) optionally a swift.properties
#      6) optionally a title.txt
#      7) preferably a README.txt
#      7) optionally a *.timeout
# template files are lightly processed by sed before use
# Missing files will be pulled from swift/etc

# WHAT TESTS ARE RUN
# Each *.swift file is a test.
# suite.sh launches all tests in each GROUP in the GROUPLIST.
# The GROUPLIST is obtained from the GROUPARG.
# 1) The GROUPARG can be an external script in the
#    groups/ subdirectory by the name of GROUPLISTFILE.
#    The GROUPLISTFILE:
#       1) sets the array
#       2) checks any variables needed by make_sites_sed()
# 2) Or, the GROUPARG can just be a directory name that is
#    the name of the singleton GROUP

# OUTPUT is the stdout of the current test
# stdout.txt retains stdout from the previous test (for *.clean.sh)
# output_*.txt is the HTML-linked permanent output from a test

# All timeouts in this script are in seconds

# PID TREE:
# Background processes are used so that hung Swift jobs can be killed
# These are the background processes (PIDs are tracked)
#
# suite.sh
# +-monitor()
#   +-sleep
# +-process_exec()
#   +-bin/swift
#     +-java
#
# PID management is now pretty good, but you may want to check ps
#  from time to time and keep xload running.
# Note that Coasters may temporarily prevent Swift from exiting upon
#  receiving a signal (cf. CoasterService.addLocalHook()).

# FAILURE CASES
# Some cases are designed to cause Swift to crash.  These
# SwiftScripts contain the token THIS-SCRIPT-SHOULD-FAIL somewhere.
# The response of suite.sh to the exit code of these Swift
# executions is reversed.

# SCHEDULERS
# Environment must contain PROJECT, QUEUE, and WORK
# These variables will be incorporated into the sites.xml
#   via make_sites_sed() -> group_sites_xml()
# Note that some schedulers restrict your choice of RUNDIR

# NAMING
# Site-specific test groups are in providers/ .
# These are named:
# providers/<provider description>/
# or:
# providers/<provider description>/<site>
# E.g., providers/local-pbs/PADS

# ADDING TESTS TO EXISTING GROUPS
# Simply add a *.swift file to a GROUP directory.
# That script will be launched when the GROUP is tested.
# Optionally, you may add helper scripts (see above) to setup,
# check, and clean up after tests.
# The helper scripts are launched from the RUNDIR and have access
# to files in RUNDIR and environment variables from suite.sh
# such as $GROUP.  Thus, you can:
#   bring in input files: cp $GROUP/input-file.txt .
#   check output:         grep TEXT1 exec.out
#                         grep TEXT2 output-file.txt
#   clean up (optional):  rm output-file.txt
# The results are added to the HTML output, etc., automatically.
# The prefix number on each test is simply for sorting
#   (e.g., ls *.swift)

# ADDING TEST GROUPS
# If no existing group has the sites, tc, etc. that you need to test,
# you will need to add a test group.  Simply create a new directory.
# Add files from TEST STRUCTURE if necessary; missing files will be
# filled in with defaults.

# IMPROVING THIS TEST SUITE
# This is a work in progress.  Here are some things you can do:
#   * Run it!  Report problems to swift-devel
#   * Fix broken tests
#   * Break down test GROUPs into smaller, meaningful GROUPs.
#     It would be good to limit GROUP sizes to 20 or so tests.
#   * Current work has focused on the HTML and stdout output,
#     which is intended to be high-level and clean. Using -v
#     results in extremely verbose output.
#     Some happy medium could be achieved by improving the use of
#     the LOG (tests.log).

# PROBLEMS
# If you have a problem:
#   * Use -v to get the set -x output.
#   * Use ps -H to get the PID tree.

# WARNINGS
# suite.sh uses shopt

shopt -s nullglob

printhelp() {
  echo "suite.sh <options> <output>"
  echo ""
  echo "usage:"
  printf "\t -a         Do not run ant dist                \n"
  printf "\t -c         Do not remove dist (clean)         \n"
  printf "\t -h         This message                       \n"
  printf "\t -k <N>     Skip first N tests                 \n"
  printf "\t -n <N>     Run N tests and quit               \n"
  printf "\t -p         Do not build the package           \n"
  printf "\t -s         Do not do a fresh svn checkout     \n"
  printf "\t -t         Tree mode (alias: -a,-c,-g,-p,-s)  \n"
  printf "\t -x         Do not continue after a failure    \n"
  printf "\t -v         Verbose (set -x, HTML comments)    \n"
  printf "\t -o output  Location for cog and output        \n"
  printf "\t <GROUP>    GROUP argument                     \n"
}

# Defaults:
DEFAULT_TIMEOUT=30 # seconds
RUN_ANT=1
CLEAN=1
SKIP_TESTS=0
NUMBER_OF_TESTS=1000000 # Run all tests by default
BUILD_PACKAGE=1
SKIP_CHECKOUT=0
ALWAYS_EXITONFAILURE=0
VERBOSE=0
# The directory in which to start:
TOPDIR=$PWD

while [ $# -gt 0 ]; do
  case $1 in
    -a)
      RUN_ANT=0
      shift;;
    -c)
      CLEAN=0
      shift;;
    -h)
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
    -v)
      VERBOSE=1
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

# Iterations per test (may want to run each test multiple times?)
ITERS_LOCAL=1

LOGCOUNT=0
SEQ=1
DATE=$( date +"%Y-%m-%d" )
TIME=$( date +"%T" )

RUNDIRBASE="run-$DATE"
RUNDIR=$TOPDIR/$RUNDIRBASE
LOGBASE=$RUNDIRBASE/tests.log
LOG=$TOPDIR/$LOGBASE
OUTPUT=$RUNDIR/exec.out

HTMLPATH=$RUNDIRBASE/tests-$DATE.html
HTML=$TOPDIR/$HTMLPATH

BRANCH=trunk
#BRANCH="branches/tests-01"

SCRIPTDIR=$( cd $( dirname $0 ) ; /bin/pwd )

TESTCOUNT=0

MASTER_PID=$$

# PIDs to kill if suite.sh is killed:
PROCESS_PID=
MONITOR_PID=

# When true, we should exit:
SHUTDOWN=0

echo "RUNNING_IN:  $RUNDIR"
echo "HTML_OUTPUT: $HTML"

TESTDIR=$TOPDIR/cog/modules/swift/tests

# Ensure all given variables are set
checkvars() {
  while (( ${#*} ))
   do
   VAR=$1
   V=$( eval "echo \${${VAR}+1}" )
   [[ $V == 1 ]] || crash "Not set: $VAR"
   shift
  done
  return 0
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
  CURRENT=$SCRIPTDIR/html/current.html
  sed "s@_HTMLBASE_@$HTMLPATH@" < $CURRENT > $TOPDIR/current.html

  HEADER=$SCRIPTDIR/html/header.html
  HOST=$( hostname )
  SEDCMD="s/_DATE_/$DATE/;s/_TIME_/$TIME/;s/_HOST_/$HOST"/
  sed $SEDCMD < $HEADER > $HTML
  FIRSTTEST=1
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

# Create HTML output
output_html() {

  TYPE=$1
  if [ "$TYPE" == "test" ]; then

    LABEL="$2"  # Text on link to output
    CMD=$3    # Command issued (td title)
    RESULT=$4 # Passed or Failed

    # WIDTH=$( width "$LABEL" )
    if [ "$RESULT" == "Passed" ]; then
      html_td class "success" width 25 title "$CMD"
      html_a_href $TEST_LOG "$LABEL"
    else
      echo "FAILED"
      cat $RUNDIR/$TEST_LOG < /dev/null
      html_td class "failure" width 25 title "$CMD"
      html_a_href $TEST_LOG $LABEL
    fi
    html_~td

  elif [ "$TYPE" == "package" ]; then
    BINPACKAGE=$2
  else
    html $@
  fi
}

start_test_results() {
  html_h1 "Test results"
  html_a_name "tests"
  html_a_href "tests.log" "Output log from tests"
  html_table border 0 cellpadding 1
}

start_group() {
  G=$1
  echo
  echo $G
  echo
  html_tr group
  html_th 3
  html "$G"
  html_~th
  html_~tr
}

start_row() {
  html_tr testline
  html_td align right width 50
  html "<b>$TESTCOUNT</b>"
  html "&nbsp;"
  html_~td
  html_td align right
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
  SEQ=1
}

end_row() {
  html_~tr
  html_~table
  html_~td
  html_~tr
}

# HTML width of label (for alignment)
width() {
  LABEL="$1"
  if [ ${#LABEL} -gt 2 ]; then
    WIDTH=""
  else
    WIDTH="width=\"20\""
  fi
  echo $WIDTH
}

# TEST_LOG = test log
test_log() {
  TEST_LOG="output_$LOGCOUNT.txt"
  banner "$LASTCMD" $RUNDIR/$TEST_LOG
  if [ -f $OUTPUT ]; then
    cp $OUTPUT $RUNDIR/$TEST_LOG 2>>$LOG
    cp $OUTPUT $RUNDIR/stdout.txt
  fi
  let "LOGCOUNT=$LOGCOUNT+1"
}

stars() {
  for i in {1..90}
  do
    printf "*"
  done
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
  # ps -H
  kill -TERM $PROCESS_INTERNAL_PID
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
  test_log
  output_html test $SEQ "$LASTCMD" $RESULT $TEST_LOG

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

  if ps | grep $PID
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
  verbose "killing monitor: $MONITOR_PID..."
  kill $MONITOR_PID

  echo "TOOK (seconds): $(( STOP-START ))"

  RESULT=$( result )
  test_log
  LASTCMD="$@"
  output_html test $SEQ "$LASTCMD" $RESULT $TEST_LOG

  check_bailout

  let "SEQ=$SEQ+1"
}

# Execute helper script (setup, check, or clean)
script_exec() {
  SCRIPT=$1
  SYMBOL="$2"

  process_exec $SCRIPT
  RESULT=$( result )

  test_log
  output_html test "$SYMBOL" "$LASTCMD" $RESULT

  check_bailout
}

# Execute Swift test case w/ setup, check, clean
swift_test_case() {
  SWIFTSCRIPT=$1
  SETUPSCRIPT=${SWIFTSCRIPT%.swift}.setup.sh
  CHECKSCRIPT=${SWIFTSCRIPT%.swift}.check.sh
  CLEANSCRIPT=${SWIFTSCRIPT%.swift}.clean.sh
  TIMEOUTFILE=${SWIFTSCRIPT%.swift}.timeout

  TEST_SHOULD_FAIL=0
  if [ -x $GROUP/$SETUPSCRIPT ]; then
    script_exec $GROUP/$SETUPSCRIPT "S"
  fi

  CDM=
  [ -r fs.data ] && CDM="-cdm.file fs.data"

  (( TESTCOUNT++ ))

  TIMEOUT=$( gettimeout $GROUP/$TIMEOUTFILE )

  grep THIS-SCRIPT-SHOULD-FAIL $SWIFTSCRIPT > /dev/null
  TEST_SHOULD_FAIL=$(( ! $?  ))

  monitored_exec $TIMEOUT swift                         \
                       -wrapperlog.always.transfer true \
                       -sitedir.keep true               \
                       -config swift.properties         \
                       -sites.file sites.xml            \
                       -tc.file tc.data                 \
                       $CDM $SWIFTSCRIPT

  TEST_SHOULD_FAIL=0
  if [ -x $GROUP/$CHECKSCRIPT ]; then
    script_exec $GROUP/$CHECKSCRIPT "&#8730;"
  fi
  if [ -x $GROUP/$CLEANSCRIPT ]; then
    script_exec $GROUP/$CLEANSCRIPT "C"
  fi
  echo
}

# Execute shell test case w/ setup, check, clean
script_test_case() {
  SHELLSCRIPT=$1
  SETUPSCRIPT=${SHELLSCRIPT%.test.sh}.setup.sh
  CHECKSCRIPT=${SHELLSCRIPT%.test.sh}.check.sh
  CLEANSCRIPT=${SHELLSCRIPT%.test.sh}.clean.sh
  TIMEOUTFILE=${SHELLSCRIPT%.test.sh}.timeout

  TEST_SHOULD_FAIL=0
  if [ -x $GROUP/$SETUPSCRIPT ]; then
    script_exec $GROUP/$SETUPSCRIPT "S"
  fi

  (( TESTCOUNT++ ))

  # Not using background for script tests yet
  # TIMEOUT=$( gettimeout $GROUP/$TIMEOUTFILE )

  if [ -x $GROUP/$SETUPSCRIPT ]; then
    script_exec $GROUP/$SETUPSCRIPT "S"
  fi

  if [ -x $GROUP/$SHELLSCRIPT ]; then
    script_exec $SHELLSCRIPT "X"
  fi

  if [ -x $GROUP/$CHECKSCRIPT ]; then
    script_exec $GROUP/$CHECKSCRIPT "&#8730;"
  fi
  if [ -x $GROUP/$CLEANSCRIPT ]; then
    script_exec $GROUP/$CLEANSCRIPT "C"
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
  output_html package "swift-$DATE.tar.gz"
}

# Generate the sites.sed file
make_sites_sed() {
  {
    echo "s@_WORK_@$WORK@"
    echo "s@_HOST_@$GLOBUS_HOSTNAME@"
    echo "s@_PROJECT_@$PROJECT@"
    echo "s@_QUEUE_@$QUEUE@"
    echo "s@_EXECUTION_URL_@$EXECUTION_URL@"
  } > $RUNDIR/sites.sed
  return 0
}

# Setup coasters variables
if which ifconfig > /dev/null 2>&1; then
  IFCONFIG=ifconfig
else
  IFCONFIG=/sbin/ifconfig
fi
$IFCONFIG > /dev/null 2>&1 || crash "Cannot run ifconfig!"
GLOBUS_HOSTNAME=$( $IFCONFIG | grep inet | head -1 | cut -d ':' -f 2 | \
                   awk '{print $1}' )
[ $? != 0 ] && crash "Could not obtain GLOBUS_HOSTNAME!"

# Generate sites.xml
group_sites_xml() {
  TEMPLATE=$GROUP/sites.template.xml
  if [ -f $TEMPLATE ]; then
    sed -f $RUNDIR/sites.sed < $TEMPLATE > sites.xml
    [ $? != 0 ] && crash "Could not create sites.xml!"
    echo "Using: $GROUP/sites.template.xml"
  else
    sed "s@_WORK_@$PWD/work@" < $TESTDIR/sites/localhost.xml > sites.xml
    [ $? != 0 ] && crash "Could not create sites.xml!"
    echo "Using: $TESTDIR/sites/localhost.xml"
  fi
}

# Generate tc.data
group_tc_data() {
  if [ -f $GROUP/tc.template.data ]; then
    sed "s@_DIR_@$GROUP@" < $GROUP/tc.template.data > tc.data
    [ $? != 0 ] && crash "Could not create tc.data!"
    echo "Using: $GROUP/tc.template.data"
  else
    cp -v $SWIFT_HOME/etc/tc.data .
    [ $? != 0 ] && crash "Could not copy tc.data!"
  fi
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
  else
    cp -v $SWIFT_HOME/etc/swift.properties .
    [ $? != 0 ] && crash "Could not copy swift.properties!"
  fi
}

# Obtain the group title
group_title() {
  if [ -r $GROUP/title.txt ]; then
    cat $GROUP/title.txt
  else
    echo "untitled"
  fi
}

# Execute all tests in current GROUP
test_group() {

  group_sites_xml
  group_tc_data
  group_fs_data
  group_swift_properties

  SWIFTS=$( echo $GROUP/*.swift )
  checkfail "Could not list: $GROUP"

  for TEST in $SWIFTS; do

    (( SKIP_COUNTER++ < SKIP_TESTS )) && continue

    TESTNAME=$( basename $TEST )

    echo -e "\nTest case: $TESTNAME"
    cp -v $GROUP/$TESTNAME .
    TESTLINK=$TESTNAME

    start_row
    for (( i=0; $i<$ITERS_LOCAL; i=$i+1 )); do
      swift_test_case $TESTNAME
      (( $TESTCOUNT >= $NUMBER_OF_TESTS )) && return
      (( $SHUTDOWN )) && return
    done
    end_row
  done

  SCRIPTS=$( echo $GROUP/*.test.sh )
  checkfail "Could not list: $GROUP"
  for TEST in $SCRIPTS; do

    (( SKIP_COUNTER++ < SKIP_TESTS )) && continue

    TESTNAME=$( basename $TEST )
    cp -v $GROUP/$TESTNAME .
    TESTLINK=$TESTNAME

    start_row
    for ((i=0; $i<$ITERS_LOCAL; i=$i+1)); do
      script_test_case $TESTNAME
      (( $TESTCOUNT >= $NUMBER_OF_TESTS )) && return
      (( $SHUTDOWN )) && return
    done
    end_row
  done
}

if [[ $WORK == "" ]]
then
  WORK=$TOPDIR/work
fi

checkvars GROUPARG
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

make_sites_sed

header
start_test_results
cd $TOPDIR

start_group "Build"

TESTLINK=
EXITONFAILURE=true
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
  (( $TESTCOUNT >= $NUMBER_OF_TESTS )) && break
  (( $SHUTDOWN )) && break
done

footer

exit 0

# Local Variables:
# sh-basic-offset: 2
# End:
