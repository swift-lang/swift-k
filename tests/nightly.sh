#!/bin/bash

# USAGE NOTES:

# Run this script from a working directory in which you
# are willing to check out the whole Swift source and
# generate many small test files.

# The script will checkout Swift, run several tests
# in a subdirectory called run-DATE, and generate
# useful HTML output and tests.log

# Run nightly.sh -h for quick help
# When something goes wrong, find and check tests.log or use -v
# Code is checked out into TOPDIR
# Swift is installed in its source tree
# The run is executed in RUNDIR (TOPDIR/RUNDIRBASE)
# The build test is started in TOPDIR
# Everything for a Swift test is written in its RUNDIR
# The temporary output always goes to OUTPUT (TOPDIR/exec.out)

# Each *.swift test may be accompanied by a
# *.setup.sh, *.check.sh, and/or *.clean.sh script
# and a *.timeout specifier
# The scripts may setup and inspect files in RUNDIR including exec.out,
# which must be accessed in stdout.txt, because the currently running
# tested process writes to exec.out, stdout.txt is a copy
# The GROUP scripts can read the GROUP variable
# The timeout number in the *.timeout file overrides the default
# timeout

# Tests are GROUPed into directories
# Each GROUP directory has:
#      1) a list of *.swift tests (plus *.sh scripts)
#      2) optionally a tc.template.data
#      3) optionally a fs.template.data
#      4) optionally a swift.properties
#      5) optionally a title.txt
#      6) preferably a README.txt
# Edit GROUPLIST at the end of this script before running

# OUTPUT is the stdout of the current test
# stdout.txt retains stdout from the previous test (for *.clean.sh)
# output_*.txt is the HTML-linked permanent output from a test

# All timeouts in this script are in seconds

# PID TREE:
# Background processes are used so that hung Swift jobs can be killed
# These are the background processes (PIDs are tracked)
# nightly.sh
# +-monitor()
#   +-sleep
# +-process_exec()
#   +-bin/swift
#     +-java

# FAILURE CASES
# Some cases are designed to cause Swift to crash.  These
# SwiftScripts contain the token THIS-SCRIPT-SHOULD-FAIL somewhere.
# The response of nightly.sh to the exit code of these Swift
# executions is reversed.

printhelp() {
  echo "nightly.sh <options> <output>"
  echo ""
  echo "usage:"
  printf "\t -a      Do not run ant dist             \n"
  printf "\t -c      Do not remove dist (clean)      \n"
  printf "\t -g      Do not run grid tests           \n"
  printf "\t -h      This message                    \n"
  printf "\t -k <N>  Skip first N tests              \n"
  printf "\t -n <N>  Run N tests and quit            \n"
  printf "\t -p      Do not build the package        \n"
  printf "\t -s      Do not do a fresh svn checkout  \n"
  printf "\t -x      Do not continue after a failure \n"
  printf "\t -v      Verbose (set -x, HTML comments) \n"
  printf "\t output  Location for output (TOPDIR)    \n"
}

# Defaults:
DEFAULT_TIMEOUT=30 # seconds
RUN_ANT=1
CLEAN=1
SKIP_TESTS=0
BUILD_PACKAGE=1
GRID_TESTS=1
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
    -g)
      GRID_TESTS=0
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
    -p)
      BUILD_PACKAGE=0
      shift;;
    -s)
      SKIP_CHECKOUT=1
      shift;;
    -x)
      ALWAYS_EXITONFAILURE=1
      shift;;
    -v)
      VERBOSE=1
      shift;;
    *)
      TOPDIR=$1
      shift;;
  esac
done

if (( VERBOSE )); then
  set -x
  HTML_COMMENTS=1
fi

# Iterations
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

SCRIPTDIR=$( dirname $0 )

SWIFTCOUNT=0

echo "HTML_OUTPUT: $HTML"

cd $TOPDIR
mkdir -p $RUNDIR
[ $? != 0 ] && echo "Could not mkdir: $RUNDIR" && exit 1

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
        OPTS+="cellpadding=\"$2\" "
        shift 2;;
      border)
        OPTS+="border=\"$2\" "
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
        OPTS+="align=\"$2\" "
        shift 2;;
      class)
        OPTS+="class=\"$2\" "
        shift 2;;
      title)
        OPTS+="title=\"$2\" "
        shift 2;;
      width)
        OPTS+="width=\"$2\" "
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
  COMMENT=$1
  (( HTML_COMMENTS == 1 )) && html "<!-- $COMMENT -->"
}

footer() {
  html "</tr></table></tr></table>"
  html_comment "End of tests"

  MONTHS=("" "Jan" "Feb" "Mar" "Apr" "May" "Jun" \
    "Jul" "Aug" "Sep" "Oct" "Nov" "Dec")

  if [ "$BINPACKAGE" != "" ]; then
    FBP=$RUNDIR/$BINPACKAGE
    SIZE=`ls -hs $FBP`
    SIZE=${SIZE/$FBP}
    cat <<DOH >>$HTML
	<h1>Binary packages</h1>
	<a name="#packages">
	<a href="$BINPACKAGE">$BINPACKAGE</a> ($SIZE)<br>
DOH
  fi

  LASTYR="00"
  LASTMO="00"
  html "<h1>Older tests</h1>"
  html '<a name="older">'
  html "<table><tr>"
  for OLDER in `ls $OUTDIR/tests-*.html|sort`; do
    O=`basename $OLDER`
    YR=${O:6:2}
    MO=${O:8:2}
    DY=${O:10:2}
    if echo "$DY$MO$YR"|egrep -v "[0-9]{6}"; then
      YR=${O#tests-}
      YR=${YR%.html}
      MO=0
      DY=$YR
    else
      YR="20$YR"
    fi
    if [ $LASTYR != $YR ]; then
      html "</tr></table>"
      html "<h2>$YR</h2>"
      LASTYR=$YR
    fi
    if [ $LASTMO != $MO ]; then
      html "</tr></table>"
      html "<h3>${MONTHS[$MO]}</h3>"
      html "<table border=\"0\"><tr>"
      LASTMO=$MO
    fi
    SUCCESS=`grep 'class="success"' $OLDER|wc -l`
    FAILURE=`grep 'class="failure"' $OLDER|wc -l`
    if [ "$SUCCESS$FAILURE" == "00" ]; then
      COLOR="#e0e0e0"
    else
      COLOR=`perl -e "printf \"#%02x%02x%02x\", $FAILURE/($SUCCESS+$FAILURE)*220+35, $SUCCESS/($SUCCESS+$FAILURE)*220+35, 40;"`
    fi
    html "<td bgcolor=\"$COLOR\"><a href=\"$O\">$DY</a></td>"
  done
  html "</tr></table><br><br>"
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

out() {
        # echo $@
  TYPE=$1
  if [ "$TYPE" == "test" ]; then

    LABEL="$2"  # Text on link to output
    CMD=$3    # Command issued (td title)
    RESULT=$4 # Passed or Failed

    # WIDTH=$( width "$LABEL" )
    if [ "$RESULT" == "Passed" ]; then
      # html "<td class=\"success\" $WIDTH title=\"$CMD\">"
      html_td class "success" width 25 title "$CMD"
      html_a_href $TEST_LOG "$LABEL"
    else
      echo "FAILED"
      cat $RUNDIR/$TEST_LOG < /dev/null
      # html "<td class=\"failure\" $WIDTH title=\"$CMD\">"
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

start_part() {
  PART=$1
  echo
  echo $PART
  echo
  html_tr part
  html_th 3
  html "$PART"
  html_~th
  html_~tr
}

start_row() {
  html_tr testline
  html_td align right width 50
  html "<b>$SWIFTCOUNT</b>"
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
    BANNER_OUTPUT=$2
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

process_exec() {
  printf "\nExecuting: $@" >>$LOG
  rm -f $OUTPUT

  "$@" > $OUTPUT 2>&1 &
  EXEC_PID=$!

  trap "process_exec_trap $EXEC_PID" SIGTERM

  wait $EXEC_PID
  EXITCODE=$?
  if [ "$EXITCODE" == "127" ]; then
    echo "Command not found: $@" > $OUTPUT
  fi
  if [ -f $OUTPUT ]; then
    cat $OUTPUT >> $LOG
  fi
  (( $TEST_SHOULD_FAIL )) && EXITCODE=$(( ! $EXITCODE ))
  return $EXITCODE
}

# Ensure we kill the tested process and any subordinate java processes
# in case of monitor() timeout
# Rationale: Killing bin/swift does not kill the Swift java process
process_exec_trap() {
  EXEC_PID=$1
  echo "process_exec_trap()"
  kill -KILL $EXEC_PID
  killall_swift
}

# Kill all subordinate swift/java processes
killall_swift() {
  echo "killing all swifts..."
  set -x
  echo $$
  ps -f
  kill_this $( ps -f | grep $$'.*'java  | grep -v grep )
  set +x
}

# Kill a process given its line output from "ps -f"
kill_this() {
  [ -n $2 ] && /bin/kill -KILL $2
}

# Execute as part of test set
# Equivalent to monitored_exec() (but w/o monitoring)
test_exec() {
  banner "$TEST (part $SEQ)"
  echo "Executing $TEST (part $SEQ)"
  process_exec "$@"
  RESULT=$( result )
  test_log
  out test $SEQ "$LASTCMD" $RESULT $TEST_LOG

  check_bailout

  let "SEQ=$SEQ+1"
}

# Background process monitoring function
# To be executed in the background as well
# If the monitored process times out, monitor() kills it and writes
# a message to OUTPUT
# If monitor() kills the process, it returns 0
# Otherwise, return non-zero (as would result from killing
# this function)
monitor() {
  (( $VERBOSE )) && set -x

  PID=$1
  TIMEOUT=$2 # seconds
  OUTPUT=$3

  V=$SWIFTCOUNT

  # Use background so kill/trap is immediate
  sleep $TIMEOUT > /dev/null 2>&1 &
  SLEEP_PID=$!
  trap "monitor_trap $SLEEP_PID $V > /dev/null 2>&1" SIGTERM
  wait $SLEEP_PID
  [ $? != 0 ] && verbose "monitor($V) cancelled" && return 0

  if ps | grep $PID
  then
    echo "monitor($V): killing test process..."
    touch killed_test
    /bin/kill -TERM $PID
    KILLCODE=$?
    if [ $KILLCODE == 0 ]; then
      echo "monitor($V): killed process_exec (TERM)"
    fi
  fi

  sleep 1
  MSG="nightly.sh: monitor($V): killed: exceeded $TIMEOUT seconds"
  echo "$MSG" >> $OUTPUT
  # trap
  return 1
}

monitor_trap() {
  SLEEP_PID=$1
  V=$2
  verbose "monitor_trap(): kill sleep"
  /bin/kill -KILL $SLEEP_PID
}

# Execute given command line in background with monitor
# Otherwise equivalent to test_exec()
# usage: monitored_exec <TIMEOUT> <command> <args>*
monitored_exec()
{
  TIMEOUT=$1
  shift

  banner "$TEST (part $SEQ)"
  echo "Executing $TEST (part $SEQ)"

  START=$( date +%s )

  process_exec "$@" &
  PROCESS_PID=$!

  monitor $PROCESS_PID $TIMEOUT $OUTPUT &
  MONITOR_PID=$!

  wait $PROCESS_PID
  EXITCODE=$?

  STOP=$( date +%s )

  # If the test was killed, monitor() may have work to do
  rm killed_test && sleep 5
  verbose "Killing monitor..."
  /bin/kill -TERM $MONITOR_PID

  echo "TOOK: $(( STOP-START ))"

  RESULT=$( result )
  test_log
  LASTCMD="$@"
  out test $SEQ "$LASTCMD" $RESULT $TEST_LOG

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
  out test "$SYMBOL" "$LASTCMD" $RESULT

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

  (( SWIFTCOUNT++ ))

  TIMEOUT=$( gettimeout $GROUP/$TIMEOUTFILE )

  grep THIS-SCRIPT-SHOULD-FAIL $SWIFTSCRIPT > /dev/null
  TEST_SHOULD_FAIL=$(( ! $?  ))

  monitored_exec $TIMEOUT swift                         \
                       -wrapperlog.always.transfer true \
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
  banner "$TEST (part $SEQ)"
  echo "Executing $TEST (part $SEQ)"
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
  out package "swift-$DATE.tar.gz"
}

GLOBUS_HOSTNAME=$( ifconfig | grep inet | head -1 | cut -d ':' -f 2 | \
                   awk '{print $1}' )
group_sites_xml() {
  TEMPLATE=$GROUP/sites.template.xml
  if [ -f $TEMPLATE ]; then
    sed "s@_WORK_@$PWD/work@;s@_HOST_@$GLOBUS_HOSTNAME@" < $TEMPLATE > sites.xml
    [ $? != 0 ] && crash "Could not create sites.xml!"
    echo "Using: $GROUP/sites.template.xml"
  else
    sed "s@_WORK_@$PWD/work@" < $TESTDIR/sites/localhost.xml > sites.xml
    [ $? != 0 ] && crash "Could not create sites.xml!"
    echo "Using: $TESTDIR/sites/localhost.xml"
  fi
}

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

group_fs_data() {
  if [ -f $GROUP/fs.template.data ]; then
    sed "s@_PWD_@$PWD@" < $GROUP/fs.template.data > fs.data
    [ $? != 0 ] && crash "Could not create fs.data!"
    echo "Using: $GROUP/fs.template.data"
  else
    rm -f fs.data
  fi
}

group_swift_properties() {
  if [ -f $GROUP/swift.properties ]; then
    cp -v $GROUP/swift.properties .
    [ $? != 0 ] && crash "Could not copy swift.properties!"
  else
    cp -v $SWIFT_HOME/etc/swift.properties .
    [ $? != 0 ] && crash "Could not copy swift.properties!"
  fi
}

group_title() {
  if [ -r $GROUP/title.txt ]; then
    cat $GROUP/title.txt
  else
    echo "untitled"
  fi
}

test_group() {

  group_sites_xml
  group_tc_data
  group_fs_data
  group_swift_properties

  SWIFTS=$( ls $GROUP/*.swift )
  checkfail "Could not ls: $GROUP"

  for TEST in $SWIFTS; do

    (( SKIP_COUNTER++ < SKIP_TESTS )) && continue

    TESTNAME=$( basename $TEST)
    cp -v $GROUP/$TESTNAME .
    TESTLINK=$TESTNAME

    start_row
    for ((i=0; $i<$ITERS_LOCAL; i=$i+1)); do
      swift_test_case $TESTNAME
      (( $SWIFTCOUNT >= $NUMBER_OF_TESTS )) && return
    done
    end_row
  done
}

date > $LOG

header
start_test_results
cd $TOPDIR

start_part "Prolog: Build"

TESTLINK=
EXITONFAILURE=true
if [ "$SKIP_CHECKOUT" != "1" ]; then
  TESTNAME="Checkout CoG"
  start_row
  test_exec rm -rf cog
  COG="https://cogkit.svn.sourceforge.net/svnroot/cogkit/trunk/current/src/cog"
  test_exec svn co $COG
  end_row

  TESTNAME="Checkout Swift"
  start_row
  test_exec cd cog/modules
  test_exec rm -rf swift
  test_exec svn co https://svn.ci.uchicago.edu/svn/vdl2/$BRANCH swift
  end_row
fi

TESTNAME="Compile"
start_row

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

PATH=$SWIFT_HOME/bin:$PATH
cd $TOPDIR
which swift
cd $RUNDIR

end_row

if [ $ALWAYS_EXITONFAILURE != "1" ]; then
  EXITONFAILURE=false
fi

TESTDIR=$TOPDIR/cog/modules/swift/tests

SKIP_COUNTER=0

GROUPLIST=( $TESTDIR/language-behaviour \
            $TESTDIR/language/working \
            $TESTDIR/local \
            $TESTDIR/language/should-not-work \
            $TESTDIR/cdm \
            $TESTDIR/cdm/ps \
            $TESTDIR/cdm/ps/pinned )

GROUPCOUNT=1
for G in ${GROUPLIST[@]}; do
  export GROUP=$G
  TITLE=$( group_title )
  start_part "Part $GROUPCOUNT: $TITLE"
  test_group
  (( GROUPCOUNT++ ))
  (( $SWIFTCOUNT >= $NUMBER_OF_TESTS )) && break
done

if [ $GRID_TESTS == "0" ]; then
  exit 0
fi

TESTPART="Appendix G: Grid Tests"

for TEST in `ls $TESTDIR/*.dtm $TESTDIR/*.swift`; do
  BN=`basename $TEST`
  echo $BN
  cp $TESTDIR/$BN .

  TESTNAME=${BN%.dtm}
  TESTNAME=${TESTNAME%.swift}
  TEST="<a href=\"$RUNDIRBASE/$BN\">$TESTNAME</a>"

  ssexec "Compile" vdlc $BN
  for ((i=0; $i<9; i=$i+1)); do
    test_exec swift -sites.file ~/.vdl2/sites-grid.xml $TESTNAME.kml
  done
  test_exec swift -sites.file ~/.vdl2/sites-grid.xml $TESTNAME.kml
done

footer

exit 0

# Local Variables:
# sh-basic-offset: 2
# End:
