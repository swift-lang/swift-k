#!/bin/bash

# USAGE NOTES:
# Run nightly.sh -h for quick help
# When something goes wrong, find and check tests.log
# Code is checked out into TOPDIR
# Swift is installed in its source tree
# The run is executed in RUNDIR (TOPDIR/RUNDIRBASE)
# The build test is started in TOPDIR
# Everything for a Swift test is written in its RUNDIR
# The temporary output always goes to OUTPUT (TOPDIR/exec.out)

printhelp() {
  echo "nightly.sh <options> <output>"
  echo ""
  echo "usage:"
  printf "\t -a      Do not run ant                  \n"
  printf "\t -c      Do not clean                    \n"
  printf "\t -g      Do not run grid tests           \n"
  printf "\t -h      This message                    \n"
  printf "\t -k <N>  Skip first N tests              \n"
  printf "\t -p      Do not build the package        \n"
  printf "\t -s      Do not do a fresh svn checkout  \n"
  printf "\t -x      Do not continue after a failure \n"
  printf "\t -v      Verbose (set -x, HTML comments) \n"
  printf "\t output  Location for output (TOPDIR)    \n"
}

# Defaults:
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
ITERS_LOCAL=2

LOGCOUNT=0
SEQ=1
DATE=$( date +"%Y-%m-%d" )
TIME=$( date +"%T" )

RUNDIRBASE="run-$DATE"
RUNDIR=$TOPDIR/$RUNDIRBASE
LOGBASE=$RUNDIRBASE/tests.log
LOG=$TOPDIR/$LOGBASE
OUTPUT=$TOPDIR/exec.out

HTMLPATH=$RUNDIRBASE/tests-$DATE.html
HTML=$TOPDIR/$HTMLPATH

BRANCH=trunk
#BRANCH="branches/tests-01"

SCRIPTDIR=$( dirname $0 )

cd $TOPDIR
mkdir -p $RUNDIR
[ $? != 0 ] && echo "Could not mkdir: $RUNDIR" && exit 1

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
  html "<table border=\"0\">"
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
  ALIGN=$1
  if [ -n $ALIGN ]; then
    html "<td align=\"$ALIGN\">"
  else
    html "<td>"
  fi
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

outecho() {
  TYPE=$1
  shift
  echo "<$TYPE>$1|$2|$3|$4|$5|$6|$7|$8|$9|"
}

out() {
        # echo $@
  TYPE=$1
  if [ "$TYPE" == "test" ]; then

    LABEL=$2  # Text on link to output
    CMD=$3    # Command issued (td title)
    RESULT=$4


    if [ "$FLUSH" == "1" ]; then
      html_~tr
      html_~table
      html_~tr
    fi

    WIDTH=$( width $LABEL )
    if [ "$RESULT" == "Passed" ]; then
      html "<td class=\"success\" $WIDTH title=\"$CMD\">"
      html_a_href $TLOG $LABEL
    else
      echo "FAILED"
      cat $TLOG < /dev/null
      html "<td class=\"failure\" $WIDTH title=\"$CMD\">"
      html_a_href $TLOG $LABEL
    fi
    html "</td>"

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
  html_table
}

start_part() {
  PART=$1
  html_tr part
  html_th 2
  html "$PART"
  html_~th
  html_~tr
}

start_row() {
  html_tr testline
  html_td right
  if [[ -n $TESTLINK ]]; then
    html_a_href $TESTLINK $TESTNAME
  else
    html $TESTNAME
  fi
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
  LABEL=$1
  if [ ${#LABEL} -gt 2 ]; then
    WIDTH=""
  else
    WIDTH="width=\"20\""
  fi
  echo $WIDTH
}

# TLOG = this (current) log
tlog() {
  TLOG="output_$LOGCOUNT.txt"
  rm -fv $TLOG
  banner "$LASTCMD" $RUNDIR/$TLOG
  if [ -f $OUTPUT ]; then
    cp -v $OUTPUT $RUNDIR/$TLOG 2>>$LOG
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
  if [ "$2" == "" ]; then
    BOUT=$LOG
  else
    BOUT=$2
  fi
  {
    echo ""
          # stars
    echo "* $1"
	  # stars
  } >>$BOUT
}

aexec() {
  declare -p PWD
  printf "\nExecuting: $@" >>$LOG
  rm -fv $OUTPUT
  LASTCMD="$@"
  "$@" > $OUTPUT 2>&1
  EXITCODE=$?
  if [ "$EXITCODE" == "127" ]; then
    echo "Command not found: $@" > $OUTPUT
  fi
  if [ -f $OUTPUT ]; then
    cat $OUTPUT >>$LOG
  fi
}

# Execute as part of test set
pexec() {
  banner "$TEST (part $SEQ)"
  echo "Executing $TEST (part $SEQ)"
  aexec "$@"
  ptest
  let "SEQ=$SEQ+1"
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
  FLUSH=0
}

# Fake exec
fexec() {
  FLUSH=1
  banner "$TEST (faked)"
  echo "Faking $TEST"
  EXITCODE=0
  LASTCMD=""
  ptest
}

ptest() {
  if [ "$EXITCODE" == "0" ]; then
    RESULT="Passed"
  else
    RESULT="Failed"
  fi
  tlog
  out test $SEQ "$LASTCMD" $RESULT $TLOG
  if [ "$EXITONFAILURE" == "true" ]; then
    if [ "$EXITCODE" != "0" ]; then
      exit $EXITCODE
    fi
  fi
}

build_package() {
  TEST="Package"
  pexec cd $SWIFT_HOME/lib
  pexec rm -f castor*.jar *gt2ft*.jar ant.jar
  pexec cd $TOPDIR
  pexec tar -pczf $RUNDIR/swift-$DATE.tar.gz $SWIFT_HOME
  out package "swift-$DATE.tar.gz"
}

date > $LOG

header
start_test_results
cd $TOPDIR

start_part "Part I: Build"

TESTLINK=
EXITONFAILURE=true
if [ "$SKIP_CHECKOUT" != "1" ]; then
  TESTNAME="Checkout CoG"
  start_row
  pexec rm -rf cog
  COG="https://cogkit.svn.sourceforge.net/svnroot/cogkit/trunk/current/src/cog"
  pexec svn co $COG
  end_row

  TESTNAME="Checkout Swift"
  start_row
  pexec cd cog/modules
  pexec rm -rf swift
  pexec svn co https://svn.ci.uchicago.edu/svn/vdl2/$BRANCH swift
  end_row
fi

TESTNAME="Compile"
start_row

pexec cd $TOPDIR/cog/modules/swift
if (( $CLEAN )); then
  pexec rm -rf dist
fi
if (( $RUN_ANT )); then
  pexec ant -quiet dist
fi
SWIFT_HOME=$TOPDIR/cog/modules/swift/dist/swift-svn

if [ $BUILD_PACKAGE = "1" ]; then
  build_package
fi

PATH=$SWIFT_HOME/bin:$PATH
cd $TOPDIR
which swift
TESTDIR=$TOPDIR/cog/modules/swift/tests
cd $RUNDIR

end_row

if [ $ALWAYS_EXITONFAILURE != "1" ]; then
  EXITONFAILURE=false
fi

sed "s@_WORK_@$PWD/work@" < $TESTDIR/sites/localhost.xml > sites.xml
sed "s@_DIR_@$TESTDIR@"   < $TESTDIR/tc.template.data    > tc.data

start_part "Part II: Local Tests"

J=0
for TEST in $( ls $TESTDIR/*.swift ); do

  (( J++ < SKIP_TESTS )) && continue

  TESTNAME=$( basename $TEST)
  cp -uv $TESTDIR/$TESTNAME .
  TESTLINK=$TESTNAME

  start_row
  for ((i=0; $i<$ITERS_LOCAL; i=$i+1)); do
    pexec swift -sites.file sites.xml -tc.file tc.data $TESTNAME
  done
  end_row
done

if [ $GRID_TESTS == "0" ]; then
  exit
fi

TESTPART="Part III: Grid Tests"

for TEST in `ls $TESTDIR/*.dtm $TESTDIR/*.swift`; do
  BN=`basename $TEST`
  echo $BN
  cp $TESTDIR/$BN .

  TESTNAME=${BN%.dtm}
  TESTNAME=${TESTNAME%.swift}
  TEST="<a href=\"$RUNDIRBASE/$BN\">$TESTNAME</a>"

  ssexec "Compile" vdlc $BN
  for ((i=0; $i<9; i=$i+1)); do
    pexec swift -sites.file ~/.vdl2/sites-grid.xml $TESTNAME.kml
  done
  pexec swift -sites.file ~/.vdl2/sites-grid.xml $TESTNAME.kml
done

#Don't remove me:
footer

# Local Variables:
# sh-basic-offset: 2
# End:
